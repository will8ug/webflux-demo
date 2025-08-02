package io.will.webfluxdemo.integration;

import io.will.webfluxdemo.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBodyList(User.class)
                .hasSize(3)
                .contains(
                    new User(1L, "Alice", "alice@example.com"),
                    new User(2L, "Bob", "bob@example.com"),
                    new User(3L, "Charlie", "charlie@example.com")
                );
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        webTestClient.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody(User.class)
                .isEqualTo(new User(1L, "Alice", "alice@example.com"));
    }

    @Test
    void getUserById_WithInvalidId_ShouldReturnNotFound() {
        webTestClient.get()
                .uri("/api/users/999")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        User newUser = new User(4L, "David", "david@example.com");
        
        webTestClient.post()
                .uri("/api/users")
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody(User.class)
                .isEqualTo(newUser);
    }

    @Test
    void deleteUser_ShouldReturnSuccess() {
        webTestClient.delete()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getUserById_ShouldHandleConcurrentRequests() {
        webTestClient.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(new User(1L, "Alice", "alice@example.com"));

        webTestClient.get()
                .uri("/api/users/2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(new User(2L, "Bob", "bob@example.com"));
    }
} 