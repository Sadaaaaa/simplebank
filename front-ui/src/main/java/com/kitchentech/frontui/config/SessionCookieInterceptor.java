package com.kitchentech.frontui.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

@Component
public class SessionCookieInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest servletRequest = attrs.getRequest();
            if (servletRequest.getCookies() != null) {
                for (var cookie : servletRequest.getCookies()) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        request.getHeaders().add("Cookie", "JSESSIONID=" + cookie.getValue());
                    }
                }
            }
        }
        return execution.execute(request, body);
    }
}
