package io.will.webfluxdemo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class SecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void publicEndpointShouldBeAccessible() {
        webTestClient.get()
                .uri("/api/auth/public")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("This is a public endpoint - no authentication required");
    }

    @Test
    void protectedEndpointShouldRequireAuthentication() {
        webTestClient.get()
                .uri("/api/auth/protected")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = "USER")
    void protectedEndpointShouldBeAccessibleWithUserRole() {
        webTestClient.get()
                .uri("/api/auth/protected")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("This is a protected endpoint - authentication required");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpointShouldBeAccessibleWithAdminRole() {
        webTestClient.get()
                .uri("/api/auth/admin")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("This is an admin endpoint - ADMIN role required");
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpointShouldBeForbiddenWithUserRole() {
        webTestClient.get()
                .uri("/api/auth/admin")
                .exchange()
                .expectStatus().isForbidden();
    }
}
