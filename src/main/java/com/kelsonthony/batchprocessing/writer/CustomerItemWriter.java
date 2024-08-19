package com.kelsonthony.batchprocessing.writer;

import com.kelsonthony.batchprocessing.exception.ApiUnavailableException;
import com.kelsonthony.batchprocessing.model.Customer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

@Component
public class CustomerItemWriter implements ItemWriter<Customer> {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public CustomerItemWriter(RestTemplate restTemplate,
                              @Value("${external.api.url}") String apiUrl,
                              Retry retry,
                              CircuitBreaker circuitBreaker) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public void write(List<? extends Customer> items) throws Exception {
        for (Customer customer : items) {
            try {
                sendCustomerToApi(customer);
            } catch (ApiUnavailableException e) {
                // Log the exception or handle it as needed
                System.err.println(e.getMessage());
            }
        }
    }

    @Retryable(value = ApiUnavailableException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendCustomerToApi(Customer customer) {
        CircuitBreaker.decorateRunnable(circuitBreaker, () -> {
            try {
                // Definir o cabeçalho Content-Type como application/json
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Criar a entidade HTTP com o cliente e os cabeçalhos
                HttpEntity<Customer> entity = new HttpEntity<>(customer, headers);

                // Fazer a requisição POST para o endpoint
                restTemplate.postForObject(apiUrl + "/api/customers", entity, Void.class);
            } catch (Exception e) {
                throw new ApiUnavailableException("Failed to send customer to API: " + customer);
            }
        }).run();
    }

    @Recover
    public void recover(ApiUnavailableException e, Customer customer) {
        // Handle the exception, maybe log it or notify an external system
        System.err.println("Failed to send customer to API after retries: " + customer);
    }
}
