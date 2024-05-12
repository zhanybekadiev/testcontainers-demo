package com.example.testcontainersdemo.consumer;

import com.example.testcontainersdemo.config.TopicNames;
import com.example.testcontainersdemo.model.Order;
import com.example.testcontainersdemo.repository.OrderEntity;
import com.example.testcontainersdemo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderConsumer {
    private final OrderRepository orderRepository;

    @KafkaListener(
            id = "demo.group",
            topics = TopicNames.ORDER_TOPIC
    )
    public void onOrderCreated(Order order) {
        log.info("ORDER CREATED:{}", order);
        orderRepository.save(new OrderEntity(order));
    }
}
