package com.test.zai.weather;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.zai.weather.config.MockClientConfig;

import lombok.SneakyThrows;

@SpringBootTest(classes = WeatherApplication.class)
@EnableCaching
@Import(MockClientConfig.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WeatherApplicationTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	@Qualifier(value = "mainClient") RestClient mainClient;

	@Autowired
	@Qualifier(value = "backupClient") RestClient backupClient;

	private static final ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	@SneakyThrows
	void setup() {
        var mainGetSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mainHeaderSpec = mock(RestClient.RequestHeadersSpec.class);
		var mainResponseSpec = mock(RestClient.ResponseSpec.class);

        lenient().when(mainGetSpec.uri(any(Function.class))).thenReturn(mainHeaderSpec);
        lenient().when(mainHeaderSpec.retrieve()).thenReturn(mainResponseSpec);

		var mainResponse = mapper.writeValueAsString(Map.of(
            "current", Map.of("temperature", 10, "wind_speed", 20)
        ));
       	lenient().when(mainResponseSpec.body(String.class)).thenReturn(mainResponse);
		when(mainClient.get()).thenReturn(mainGetSpec);

		
		var backupGetSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var backupHeaderSpec = mock(RestClient.RequestHeadersSpec.class);
		var backupResponseSpec = mock(RestClient.ResponseSpec.class);

        lenient().when(backupGetSpec.uri(any(Function.class))).thenReturn(backupHeaderSpec);
        lenient().when(backupHeaderSpec.retrieve()).thenReturn(backupResponseSpec);

		var backupResponse = mapper.writeValueAsString(Map.of(
            "main", Map.of("temp", 100),
			"wind", Map.of("speed", 200)
        ));
       	lenient().when(backupResponseSpec.body(String.class)).thenReturn(backupResponse);
		when(backupClient.get()).thenReturn(backupGetSpec);
	}

	@Test
	void contextLoads() {
	}

	@Test
	@SneakyThrows
	void success_call_using_mainAPI() {
		mockMvc.perform(
			MockMvcRequestBuilders.get("/v1/weather")
			.param("city", "melbourne")
		).andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.wind_speed").value("20"))
		.andExpect(MockMvcResultMatchers.jsonPath("$.temperature_degrees").value("10"));
	
		verify(mainClient, times(1)).get();
		verify(backupClient, times(0)).get();
	}

	@Test
	@SneakyThrows
	void success_call_using_backupAPI() {
		when(mainClient.get()).thenReturn(null);

		mockMvc.perform(
			MockMvcRequestBuilders.get("/v1/weather")
			.param("city", "melbourne")
		).andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.wind_speed").value("200"))
		.andExpect(MockMvcResultMatchers.jsonPath("$.temperature_degrees").value("100"));
	
		verify(mainClient, times(3)).get();
		verify(backupClient, times(1)).get();
	}

	@Test
	@SneakyThrows
	void success_call_with_default_city() {
		mockMvc.perform(
			MockMvcRequestBuilders.get("/v1/weather")
		).andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.wind_speed").value("20"))
		.andExpect(MockMvcResultMatchers.jsonPath("$.temperature_degrees").value("10"));
	
		verify(mainClient, times(1)).get();
		verify(backupClient, times(0)).get();
	}

	@Test
	@SneakyThrows
	void success_call_using_cache_response() {
		mockMvc.perform(
			MockMvcRequestBuilders.get("/v1/weather")
			.param("city", "melbourne")
		).andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.wind_speed").value("20"))
		.andExpect(MockMvcResultMatchers.jsonPath("$.temperature_degrees").value("10"));

		mockMvc.perform(
			MockMvcRequestBuilders.get("/v1/weather")
			.param("city", "melbourne")
		).andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.wind_speed").value("20"))
		.andExpect(MockMvcResultMatchers.jsonPath("$.temperature_degrees").value("10"));
	
		verify(mainClient, times(1)).get();
		verify(backupClient, times(0)).get();
	}

	@Test
	@SneakyThrows
	void exception_encountered_on_both_api() {
		when(mainClient.get()).thenReturn(null);
		when(backupClient.get()).thenReturn(null);
		
		mockMvc.perform(
			MockMvcRequestBuilders.get("/v1/weather")
			.param("city", "melbourne")
		).andExpect(MockMvcResultMatchers.status().isInternalServerError());
	
		verify(mainClient, times(3)).get();
		verify(backupClient, times(1)).get();
	}
}
