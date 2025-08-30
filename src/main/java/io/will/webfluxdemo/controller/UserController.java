package io.will.webfluxdemo.controller;

import io.will.webfluxdemo.model.ExportResult;
import io.will.webfluxdemo.model.User;
import io.will.webfluxdemo.service.DataExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private DataExportService dataExportService;

    private final List<User> users = Arrays.asList(
        new User(1L, "Alice", "alice@example.com"),
        new User(2L, "Bob", "bob@example.com"),
        new User(3L, "Charlie", "charlie@example.com")
    );

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> getAllUsers() {
        return Flux.fromIterable(users)
                .delayElements(Duration.ofMillis(100))
                .doOnNext(user -> System.out.println("Streaming user: " + user.getName()));
    }

    @GetMapping("/{id}")
    public Mono<User> getUserById(@PathVariable Long id) {
        return Mono.justOrEmpty(users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst());
    }

    @PostMapping
    public Mono<User> createUser(@RequestBody User user) {
        return Mono.just(user)
                .delayElement(Duration.ofMillis(200));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return Mono.empty();
    }

    @GetMapping("/test-error")
    public Mono<String> testError() {
        throw new RuntimeException("This is a test error for demonstration");
    }

    @GetMapping("/test-bad-request")
    public Mono<String> testBadRequest() {
        throw new IllegalArgumentException("Invalid parameter provided");
    }

    // Long-running async operations using SSE for Mono
    // This is where Mono + SSE makes sense!
    
    /**
     * Export user data - long running operation
     * Uses SSE because the operation takes significant time (7+ seconds)
     * Client can maintain connection and get result when ready
     */
    @GetMapping(path = "/export/{requestId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ExportResult> exportUsers(@PathVariable Long requestId) {
        return dataExportService.exportAsync(requestId);
    }

    /**
     * Alternative reactive export implementation
     * Shows fully non-blocking reactive processing
     */
    @GetMapping(path = "/export-reactive/{requestId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ExportResult> exportUsersReactive(@PathVariable Long requestId) {
        return dataExportService.exportAsyncReactive(requestId);
    }

    /**
     * Batch export with progress tracking
     * Suitable for very large datasets
     */
    @GetMapping(path = "/export-batch/{requestId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ExportResult> exportUsersBatch(@PathVariable Long requestId) {
        return dataExportService.exportWithProgress(requestId);
    }
}