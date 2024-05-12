package com.example.testcontainersdemo.model;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record PageQuery(int page, int size) {
    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }
}
