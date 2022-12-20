package com.kelsonthony.batchprocessing.config;

import com.kelsonthony.batchprocessing.entity.Customer;
import org.springframework.batch.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer, Customer> {
    @Override
    public Customer process(Customer customer) throws Exception {
        /*if (customer.getCountry().equals("United States")) {
            return  customer;
        } else {
            return null;
        }*/
        int age = Integer.parseInt(customer.getAge());
        if (age >= 18) {
            return  customer;
        } else {
            return null;
        }

        //return customer;
    }
}
