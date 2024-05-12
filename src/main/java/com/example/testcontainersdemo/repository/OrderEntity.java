package com.example.testcontainersdemo.repository;

import com.example.testcontainersdemo.model.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "amount")
    private BigDecimal amount;

    public OrderEntity(Order o) {
        productName = o.productName();
        customerName = o.customerName();
        customerPhone = o.customerPhone();
        amount = o.amount();
    }
}
