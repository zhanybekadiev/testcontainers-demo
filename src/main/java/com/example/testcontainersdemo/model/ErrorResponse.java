package com.example.testcontainersdemo.model;

import java.time.Instant;

public record ErrorResponse(String message, Instant ts)  {
}
