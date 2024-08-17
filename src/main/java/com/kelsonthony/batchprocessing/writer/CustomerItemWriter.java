package com.kelsonthony.batchprocessing.writer;

import com.kelsonthony.batchprocessing.model.Customer;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Component
public class CustomerItemWriter implements ItemWriter<Customer> {

    private final RestTemplate restTemplate;
    private final String apiUrl;

    public CustomerItemWriter(RestTemplate restTemplate,
                              @Value("${external.api.url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    @Override
    public void write(List<? extends Customer> customers) throws Exception {
        System.out.println("Thread Name: " + Thread.currentThread().getName());
        System.out.println("hello customers" + customers);

        for (Customer customer : customers) {
            sendCustomerToApi(customer);
        }

    }

    private void sendCustomerToApi(Customer customer) {
        try {
            restTemplate.postForObject(apiUrl, customer, String.class);
            System.out.println("Sent customer to API: " + customer);
        } catch (Exception e) {
            System.err.println("Failed to send customer to API: " + customer);
            e.printStackTrace();
        }
    }
}
