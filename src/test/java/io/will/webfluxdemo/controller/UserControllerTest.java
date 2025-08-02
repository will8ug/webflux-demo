package io.will.webfluxdemo.controller;

import io.will.webfluxdemo.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(UserController.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getAllUsers_ShouldReturnFluxOfUsers() {
        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(3);
    }

    @Test
    void getUserById_ShouldReturnMonoOfUser() {
        webTestClient.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(new User(1L, "Alice", "alice@example.com"));
    }

    @Test
    void createUser_ShouldReturnMonoOfCreatedUser() {
        User newUser = new User(4L, "David", "david@example.com");
        
        webTestClient.post()
                .uri("/api/users")
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(newUser);
    }
} 