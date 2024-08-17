package com.kelsonthony.batchprocessing.processor;

import com.kelsonthony.batchprocessing.model.Customer;
import org.springframework.batch.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer, Customer> {
    @Override
    public Customer process(Customer customer) throws Exception {

        int age = Integer.parseInt(customer.getAge());
        if (age >= 40) {
            System.out.println("hello processor!!!" + age);
            return  customer;
        } else {
            return null;
        }

    }
}
