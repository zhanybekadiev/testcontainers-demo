package com.example.testcontainersdemo.consumer;

import com.example.testcontainersdemo.AbstractIntegrationTest;
import com.example.testcontainersdemo.OrderTestUtils;
import com.example.testcontainersdemo.config.TopicNames;
import com.example.testcontainersdemo.model.Order;
import com.example.testcontainersdemo.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest
class OrderConsumerTest extends AbstractIntegrationTest {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private OrderRepository orderRepository;
    private static final Random random = new Random(1);

    @Test
    public void shouldCreateOrder() {
        Order newOrder = OrderTestUtils.generateOrder(random);
        kafkaTemplate.send(TopicNames.ORDER_TOPIC, newOrder);

        waitAtMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var orderOpt = orderRepository.findByCustomerName(newOrder.customerName());
                    assertThat(orderOpt).isPresent();

                    var order = orderOpt.get();
                    assertThat(order.getCustomerName()).isEqualTo(newOrder.customerName());
                    assertThat(order.getCustomerName()).isEqualTo(newOrder.customerName());
                    assertThat(order.getCustomerPhone()).isEqualTo(newOrder.customerPhone());
                    assertThat(order.getAmount()).isEqualByComparingTo(newOrder.amount());
                });
    }
}