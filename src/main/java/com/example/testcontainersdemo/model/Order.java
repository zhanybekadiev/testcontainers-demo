package com.example.testcontainersdemo.model;

import com.example.testcontainersdemo.repository.OrderEntity;

import java.math.BigDecimal;

public record Order(Long id, String productName, String customerName, String customerPhone, BigDecimal amount) {
    public Order(OrderEntity order) {
        this(order.getId(), order.getProductName(), order.getCustomerName(), order.getCustomerPhone(), order.getAmount());
    }
}

