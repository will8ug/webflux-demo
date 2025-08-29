package io.will.webfluxdemo.integration;

import io.will.webfluxdemo.model.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
class UserIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @Tag("Basic")
    void getAllUsers_ShouldReturnAllUsers() {
        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(User.class)
                .hasSize(3)
                .contains(
                    new User(1L, "Alice", "alice@example.com"),
                    new User(2L, "Bob", "bob@example.com"),
                    new User(3L, "Charlie", "charlie@example.com")
                );
    }

    @Test
    @Tag("Basic")
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
    @Tag("Basic")
    void getUserById_WithInvalidId_ShouldReturnNotFound() {
        webTestClient.get()
                .uri("/api/users/999")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    @Tag("Basic")
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
    @Tag("Basic")
    void deleteUser_ShouldReturnSuccess() {
        webTestClient.delete()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @Tag("Basic")
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

    // Integration tests corresponding to the curl commands in ERROR_HANDLING_GUIDE.md

    @Test
    @Tag("ErrorHandling")
    void testError_ShouldReturn500WithCustomExceptionHandler() {
        webTestClient.get()
                .uri("/api/users/test-error")
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.exception").isEqualTo("RuntimeException")
                .jsonPath("$.message").isEqualTo("This is a test error for demonstration")
                .jsonPath("$.path").isEqualTo("/api/users/test-error")
                .jsonPath("$.method").isEqualTo("GET")
                .jsonPath("$.requestId").exists()
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @Tag("ErrorHandling")
    void testBadRequest_ShouldReturn400WithCustomExceptionHandler() {
        webTestClient.get()
                .uri("/api/users/test-bad-request")
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.exception").isEqualTo("IllegalArgumentException")
                .jsonPath("$.message").isEqualTo("Invalid parameter provided")
                .jsonPath("$.path").isEqualTo("/api/users/test-bad-request")
                .jsonPath("$.method").isEqualTo("GET")
                .jsonPath("$.requestId").exists()
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @Tag("ErrorHandling")
    void nonexistentEndpoint_ShouldReturn500WithNotFoundInMessage() {
        webTestClient.get()
                .uri("/nonexistent-endpoint")
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.exception").isEqualTo("ResponseStatusException")
                .jsonPath("$.message").isEqualTo("404 NOT_FOUND")
                .jsonPath("$.path").isEqualTo("/nonexistent-endpoint")
                .jsonPath("$.method").isEqualTo("GET")
                .jsonPath("$.requestId").exists()
                .jsonPath("$.timestamp").exists();
    }
} 