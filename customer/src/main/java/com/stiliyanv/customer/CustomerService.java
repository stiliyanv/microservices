package com.stiliyanv.customer;

import com.stiliyanv.amqp.RabbitMQMessageProducer;
import com.stiliyanv.clients.fraud.FraudCheckResponse;
import com.stiliyanv.clients.fraud.FraudClient;
import com.stiliyanv.clients.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;
    private final RabbitMQMessageProducer producer;

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

        final NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s and welcome...",
                        customer.getFirstName())
        );

        // async - add it to the queue (rabbitmq)
        producer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key");

    }
}