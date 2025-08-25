package io.will.webfluxdemo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public Mono<Map<String, Object>> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(this::createUserInfo);
    }

    @GetMapping("/public")
    public Mono<String> publicEndpoint() {
        return Mono.just("This is a public endpoint - no authentication required");
    }

    @GetMapping("/protected")
    public Mono<String> protectedEndpoint() {
        return Mono.just("This is a protected endpoint - authentication required");
    }

    @GetMapping("/admin")
    public Mono<String> adminEndpoint() {
        return Mono.just("This is an admin endpoint - ADMIN role required");
    }

    private Map<String, Object> createUserInfo(Authentication authentication) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("authorities", authentication.getAuthorities());
        userInfo.put("authenticated", authentication.isAuthenticated());
        return userInfo;
    }
}
