package io.will.webfluxdemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.WebFilter;

import java.util.UUID;

@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebFluxConfig.class);

    @Bean
    public WebFilter requestLoggingFilter() {
        return (exchange, chain) -> {
            String requestId = UUID.randomUUID().toString();
            exchange.getAttributes().put("requestId", requestId);
            
            // Add request ID to response headers
            exchange.getResponse().getHeaders().add("X-Request-ID", requestId);
            
            String path = exchange.getRequest().getPath().value();
            String method = exchange.getRequest().getMethod().name();
            String remoteAddress = exchange.getRequest().getRemoteAddress() != null ? 
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            
            long startTime = System.currentTimeMillis();
            
            logger.info("=== REQUEST START ===");
            logger.info("Request ID: {}", requestId);
            logger.info("Path: {} {}", method, path);
            logger.info("Remote Address: {}", remoteAddress);
            logger.info("User-Agent: {}", exchange.getRequest().getHeaders().getFirst("User-Agent"));
            logger.info("Content-Type: {}", exchange.getRequest().getHeaders().getContentType());
            
            return chain.filter(exchange)
                    .doFinally(signalType -> {
                        long duration = System.currentTimeMillis() - startTime;
                        int status = exchange.getResponse().getStatusCode() != null ? 
                            exchange.getResponse().getStatusCode().value() : 0;
                        
                        logger.info("=== REQUEST END ===");
                        logger.info("Request ID: {}", requestId);
                        logger.info("Status: {}", status);
                        logger.info("Duration: {}ms", duration);
                        logger.info("==================");
                    });
        };
    }

    @Bean
    public WebFilter errorLoggingFilter() {
        return (exchange, chain) -> {
            return chain.filter(exchange)
                    .doOnError(throwable -> {
                        String requestId = (String) exchange.getAttributes().get("requestId");
                        String path = exchange.getRequest().getPath().value();
                        String method = exchange.getRequest().getMethod().name();
                        
                        logger.error("=== ERROR OCCURRED ===");
                        logger.error("Request ID: {}", requestId);
                        logger.error("Path: {} {}", method, path);
                        logger.error("Error Type: {}", throwable.getClass().getSimpleName());
                        logger.error("Error Message: {}", throwable.getMessage());
                        logger.error("Stack Trace:", throwable);
                        logger.error("=====================");
                    });
        };
    }
}
