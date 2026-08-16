package com.matheus.srv_portfolio_scheduler.adapters.input.filter;

import com.matheus.srv_portfolio_scheduler.application.utils.CorrelationId;
import jakarta.servlet.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CorrelationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        try {
            CorrelationId.generate();
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            CorrelationId.clear();
        }
    }
}
