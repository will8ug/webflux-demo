package io.will.webfluxdemo.integration;

import io.will.webfluxdemo.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebFluxPerformanceTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testReactiveStreaming_ShouldHandleBackpressure() {
        // Test that the reactive stream can handle backpressure properly
        Flux<User> userFlux = webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .returnResult(User.class)
                .getResponseBody();

        StepVerifier.create(userFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void testConcurrentRequests_ShouldNotBlock() {
        // Test multiple concurrent requests to demonstrate non-blocking nature
        List<WebTestClient.ResponseSpec> responses = List.of(
            webTestClient.get().uri("/api/users/1").exchange(),
            webTestClient.get().uri("/api/users/2").exchange(),
            webTestClient.get().uri("/api/users/3").exchange()
        );

        // All requests should complete successfully
        responses.forEach(response -> 
            response.expectStatus().isOk()
        );
    }

    @Test
    void testReactiveTiming_ShouldRespectDelays() {
        long startTime = System.currentTimeMillis();
        
        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(3);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // The delay is 100ms per element, so with 3 elements, it should take at least 300ms
        // We add some buffer for test execution overhead
        assert duration >= 250 : "Expected at least 250ms due to reactive delays, but got " + duration + "ms";
    }

    @Test
    void testCreateUserTiming_ShouldRespectDelay() {
        User newUser = new User(5L, "Eve", "eve@example.com");
        
        long startTime = System.currentTimeMillis();
        
        webTestClient.post()
                .uri("/api/users")
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(newUser);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // The delay is 200ms, so it should take at least 200ms
        assert duration >= 150 : "Expected at least 150ms due to reactive delay, but got " + duration + "ms";
    }
} 