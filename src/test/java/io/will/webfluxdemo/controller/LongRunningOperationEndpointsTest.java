package io.will.webfluxdemo.controller;

import io.will.webfluxdemo.config.SecurityConfig;
import io.will.webfluxdemo.model.ExportResult;
import io.will.webfluxdemo.service.DataExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class LongRunningOperationEndpointsTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DataExportService dataExportService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportUsers_ShouldReturnExportResult() {
        Long requestId = 123L;
        ExportResult expectedResult = new ExportResult(
            requestId,
            "user_export_123_1634567890.csv",
            "/api/downloads/user_export_123_1634567890.csv",
            10L,
            "COMPLETED",
            LocalDateTime.now(),
            500L
        );

        when(dataExportService.exportAsync(any(Long.class)))
            .thenReturn(Mono.just(expectedResult).delayElement(Duration.ofSeconds(2)));

        webTestClient.get()
                .uri("/api/users/export/" + requestId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ExportResult.class)
                .hasSize(1)
                .value(results -> {
                    ExportResult result = results.get(0);
                    assert result.getTaskId().equals(requestId);
                    assert result.getFileName().equals("user_export_123_1634567890.csv");
                    assert result.getStatus().equals("COMPLETED");
                    assert result.getTotalRecords().equals(10L);
                });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportUsersReactive_ShouldReturnExportResult() {
        Long requestId = 456L;
        ExportResult expectedResult = new ExportResult(
            requestId,
            "user_export_reactive_456_1634567890.csv",
            "/api/downloads/user_export_reactive_456_1634567890.csv",
            10L,
            "COMPLETED",
            LocalDateTime.now(),
            500L
        );

        when(dataExportService.exportAsyncReactive(any(Long.class)))
            .thenReturn(Mono.just(expectedResult));

        webTestClient.get()
                .uri("/api/users/export-reactive/" + requestId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ExportResult.class)
                .hasSize(1)
                .value(results -> {
                    ExportResult result = results.get(0);
                    assert result.getTaskId().equals(requestId);
                    assert result.getFileName().equals("user_export_reactive_456_1634567890.csv");
                    assert result.getStatus().equals("COMPLETED");
                    assert result.getTotalRecords().equals(10L);
                });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportUsersBatch_ShouldReturnExportResult() {
        Long requestId = 789L;
        ExportResult expectedResult = new ExportResult(
            requestId,
            "batch_export_789_1634567890.csv",
            "/api/downloads/batch_export_789_1634567890.csv",
            10L,
            "COMPLETED",
            LocalDateTime.now(),
            750L
        );

        when(dataExportService.exportWithProgress(any(Long.class)))
            .thenReturn(Mono.just(expectedResult));

        webTestClient.get()
                .uri("/api/users/export-batch/" + requestId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ExportResult.class)
                .hasSize(1)
                .value(results -> {
                    ExportResult result = results.get(0);
                    assert result.getTaskId().equals(requestId);
                    assert result.getFileName().equals("batch_export_789_1634567890.csv");
                    assert result.getStatus().equals("COMPLETED");
                    assert result.getTotalRecords().equals(10L);
                    assert result.getFileSizeBytes().equals(750L);
                });
    }
}