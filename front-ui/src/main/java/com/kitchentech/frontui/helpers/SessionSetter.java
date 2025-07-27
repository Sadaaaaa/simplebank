package com.kitchentech.frontui.helpers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

public class SessionSetter {

    public static HttpHeaders createProxyHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        String[] headersToProxy = {"Cookie", "Authorization", "X-Forwarded-For", "X-Real-IP"};

        for (String headerName : headersToProxy) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                headers.add(headerName, headerValue);
            }
        }

        return headers;
    }
}
