package com.stiliyanv.customer;

import com.stiliyanv.clients.fraud.FraudCheckResponse;
import com.stiliyanv.clients.fraud.FraudClient;
import com.stiliyanv.clients.notification.NotificationClient;
import com.stiliyanv.clients.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RestTemplate restTemplate;
    private final FraudClient fraudClient;
    private final NotificationClient notificationClient;

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();
        // TODO check if email is valid
        // TODO check if email is not taken
        customerRepository.saveAndFlush(customer);

        FraudCheckResponse response =
                fraudClient.isFraudster(customer.getId());

        if (response.isFraudster()) {
            throw new IllegalStateException("Fraudster !!!");
        }

        // TODO make it async, i.e. add to queue
        notificationClient.sendNotification(
                new NotificationRequest(
                        customer.getId(),
                        customer.getEmail(),
                        String.format("Hi %s and welcome...",
                                customer.getFirstName())
                )
        );

    }
}