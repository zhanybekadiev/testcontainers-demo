package com.example.testcontainersdemo;

import com.example.testcontainersdemo.model.Order;
import com.example.testcontainersdemo.repository.OrderEntity;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class OrderTestUtils {
    public static List<OrderEntity> generateOrderEntities(int count, Random random) {
        List<OrderEntity> orders = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            orders.add(new OrderEntity(generateOrder(random)));
        }

        return orders;
    }

    public static Order generateOrder(Random random) {
        return new Order(
                null,
                randomAlphabetic(10, random),
                randomAlphabetic(12, random),
                randomNumeric(10, random),
                new BigDecimal(random.nextInt(10000))
        );
    }

    private static String randomAlphabetic(int count, Random random) {
        return RandomStringUtils.random(count, 0, 0, true, false, null, random);
    }

    private static String randomNumeric(int count, Random random) {
        return RandomStringUtils.random(count, 0, 0, false, true, null, random);
    }
}
