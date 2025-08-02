package io.will.webfluxdemo.controller;

import io.will.webfluxdemo.model.User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final List<User> users = Arrays.asList(
        new User(1L, "Alice", "alice@example.com"),
        new User(2L, "Bob", "bob@example.com"),
        new User(3L, "Charlie", "charlie@example.com")
    );

    @GetMapping
    public Flux<User> getAllUsers() {
        return Flux.fromIterable(users)
                .delayElements(Duration.ofMillis(100));
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
} 