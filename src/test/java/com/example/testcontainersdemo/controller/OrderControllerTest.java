package com.example.testcontainersdemo.controller;

import com.example.testcontainersdemo.AbstractIntegrationTest;
import com.example.testcontainersdemo.OrderTestUtils;
import com.example.testcontainersdemo.repository.OrderEntity;
import com.example.testcontainersdemo.repository.OrderRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest extends AbstractIntegrationTest {
    @LocalServerPort
    private Integer port;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MessageSourceAccessor messages;
    private static final Random random = new Random(1);

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        orderRepository.deleteAll();
        orderRepository.flush();
    }

    @Test
    void shouldCreateOrder() {
        var payload = OrderTestUtils.generateOrder(random);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/orders")
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        waitAtMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var orderOpt = orderRepository.findByCustomerName(payload.customerName());
                    assertThat(orderOpt).isPresent();

                    var order = orderOpt.get();

                    assertThat(order.getCustomerName()).isEqualTo(payload.customerName());
                    assertThat(order.getCustomerPhone()).isEqualTo(payload.customerPhone());
                    assertThat(order.getAmount()).isEqualByComparingTo(payload.amount());
                });
    }

    @Test
    void shouldGetOrders() {
        var saved = orderRepository.saveAndFlush(new OrderEntity(OrderTestUtils.generateOrder(random)));

        given()
                .contentType(ContentType.JSON)
                .params("size", 20, "page", 0)
                .when()
                .get("/orders")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(
                        "id", hasItem(saved.getId().intValue()),
                        "productName", hasItem(saved.getProductName()),
                        "customerName", hasItem(saved.getCustomerName()),
                        "customerPhone", hasItem(saved.getCustomerPhone()),
                        "amount", hasItem(saved.getAmount().floatValue())
                );
    }

    @Test
    void shouldGetBadRequest() {
        int pageSize = random.nextInt(1001, Integer.MAX_VALUE);
        int page = random.nextInt(Integer.MAX_VALUE);
        String expectedMessage = messages.getMessage("validation.page.size", new Integer[] {pageSize});

        given()
                .contentType(ContentType.JSON)
                .params("size", pageSize, "page", page)
                .when()
                .get("/orders")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", equalTo(expectedMessage));
    }

    @Test
    void shouldPaginate() {
        int cnt = 100;

        orderRepository.saveAllAndFlush(OrderTestUtils.generateOrderEntities(cnt, random));
        int pageSize = random.nextInt(1, cnt);
        int page = random.nextInt(cnt / pageSize);

        given()
                .contentType(ContentType.JSON)
                .params("size", pageSize, "page", page)
                .when()
                .get("/orders")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(pageSize));
    }
}