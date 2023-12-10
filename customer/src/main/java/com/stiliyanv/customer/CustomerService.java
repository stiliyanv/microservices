package com.stiliyanv.customer;

import com.stiliyanv.clients.fraud.FraudCheckResponse;
import com.stiliyanv.clients.fraud.FraudClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RestTemplate restTemplate;
    private final FraudClient fraudClient;

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();
        // todo check if email is valid
        // todo check if email is not taken
        customerRepository.saveAndFlush(customer);

        FraudCheckResponse response =
                fraudClient.isFraudster(customer.getId());

        if (response.isFraudster()) {
            throw new IllegalStateException("Fraudster !!!");
        }

        // todo send notificaiton
    }
}