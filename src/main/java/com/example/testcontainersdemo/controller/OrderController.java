package com.example.testcontainersdemo.controller;

import com.example.testcontainersdemo.config.TopicNames;
import com.example.testcontainersdemo.model.ErrorResponse;
import com.example.testcontainersdemo.model.Order;
import com.example.testcontainersdemo.model.PageQuery;
import com.example.testcontainersdemo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final int MAX_PAGE_SIZE = 100;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderRepository orderRepository;
    private final MessageSourceAccessor messages;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void create(@RequestBody Order order) {
        kafkaTemplate.send(TopicNames.ORDER_TOPIC, order);
    }

    @GetMapping
    public List<Order> getOrders(PageQuery query) {

        if (query.size() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    messages.getMessage("validation.page.size", new Integer[] {query.size()})
            );
        }

        return orderRepository.findAll(query.toPageable())
                .stream()
                .map(Order::new)
                .toList();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(IllegalArgumentException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(e.getMessage(), Instant.now());
    }
}
