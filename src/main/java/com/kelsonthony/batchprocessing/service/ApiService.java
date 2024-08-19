package com.kelsonthony.batchprocessing.service;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;

//import org.springframework.retry.annotation.CircuitBreaker;
//import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private final RestTemplate restTemplate;

    @Value("${external.api.url}")
    private String apiUrl;

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "externalApiCircuitBreaker", fallbackMethod = "fallbackForExternalApi")
    public String callExternalApi(String request) {
        return restTemplate.postForObject(apiUrl, request, String.class);
    }

    public String fallbackForExternalApi(String request, Throwable throwable) {
        // Resposta padrão em caso de falha
        return "Fallback response due to: " + throwable.getMessage();
    }
}