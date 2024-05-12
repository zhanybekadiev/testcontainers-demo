package com.example.testcontainersdemo.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.function.Predicate;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {
    private static final int MAX_PAYLOAD_LENGTH = 5120;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        try {
            MDC.put("requestId", UUID.randomUUID().toString());
            filterChain.doFilter(request, response);
        } finally {
            log(request, response);
            updateResponse(response);
            MDC.clear();
        }
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        responseWrapper.copyBodyToResponse();
    }

    private void log(HttpServletRequest requestToCache, HttpServletResponse responseToCache) {
        log.info(createRequestMessage(requestToCache));
        log.info(createResponseMessage(requestToCache, responseToCache));
    }

    private String getPayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);

        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH);
                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    return null;
                }
            }
        }

        return null;
    }

    private String getPayload(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper =
                WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH);
                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    return null;
                }
            }
        }

        return null;
    }

    protected String createResponseMessage(HttpServletRequest request, HttpServletResponse response) {
        var msg = new StringBuilder("Response[path=");
        msg.append(request.getRequestURI());
        msg.append(", status=").append(response.getStatus());

        String payload = getPayload(response);
        if (payload != null) {
            msg.append(", payload=").append(payload);
        }

        return msg.append(']').toString();
    }

    protected String createRequestMessage(HttpServletRequest request) {
        var msg = new StringBuilder();
        msg.append("Request[method=");
        msg.append(request.getMethod());
        msg.append(", path=").append(request.getRequestURI());

        String queryString = request.getQueryString();
        if (queryString != null) {
            msg.append('?').append(queryString);
        }

        String client = request.getRemoteAddr();
        if (StringUtils.hasLength(client)) {
            msg.append(", client=").append(client);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            msg.append(", session=").append(session.getId());
        }

        String user = request.getRemoteUser();
        if (user != null) {
            msg.append(", user=").append(user);
        }

        String payload = getPayload(request);

        if (payload != null) {
            msg.append(", payload=").append(payload);
        }

        HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String header = names.nextElement();
            if (!getHeaderPredicate().test(header)) {
                headers.set(header, "[secured]");
            }
        }

        msg.append(", headers=").append(headers);
        msg.append(']');
        return msg.toString();
    }

    private Predicate<String> getHeaderPredicate() {
        return (header) -> !header.equals(HttpHeaders.AUTHORIZATION) && !header.equals("password");
    }
}