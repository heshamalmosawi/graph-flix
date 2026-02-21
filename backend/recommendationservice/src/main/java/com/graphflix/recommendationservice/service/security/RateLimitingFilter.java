package com.graphflix.recommendationservice.service.security;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter implements Filter {

    private static class RequestInfo {
        AtomicInteger requestCount = new AtomicInteger(0);
        long timestamp = System.currentTimeMillis();
    }

    private final Map<String, RequestInfo> requestCountsPerIpAddress = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS_PER_MINUTE = 15;
    private static final long ONE_MINUTE = 60 * 1000;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String clientIpAddress = httpServletRequest.getRemoteAddr();
        RequestInfo requestInfo = requestCountsPerIpAddress.computeIfAbsent(clientIpAddress, k -> new RequestInfo());

        synchronized (requestInfo) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - requestInfo.timestamp > ONE_MINUTE) {
                requestInfo.requestCount.set(0);
                requestInfo.timestamp = currentTime;
            }

            if (requestInfo.requestCount.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                httpServletResponse.setStatus(429);
                httpServletResponse.getWriter().write("Too many requests. Please try again later.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
