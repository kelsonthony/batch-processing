package com.kelsonthony.batchprocessing.writer;

import com.kelsonthony.batchprocessing.model.Customer;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class CustomerItemWriter implements ItemWriter<Customer> {


    @Override
    public void write(List<? extends Customer> customers) throws Exception {
        System.out.println("Thread Name: " + Thread.currentThread().getName());
        System.out.println("hello customers" + customers);
        //customerRepository.saveAll(list);
    }
}
