package com.kelsonthony.batchprocessing.config;

import com.kelsonthony.batchprocessing.entity.Customer;
import com.kelsonthony.batchprocessing.repository.CustomerRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class CustomerItemWriter implements ItemWriter<Customer> {
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public void write(List<? extends Customer> list) throws Exception {
        System.out.println("Thread Name: " + Thread.currentThread().getName());
        customerRepository.saveAll(list);
    }
}
