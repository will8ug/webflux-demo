package io.will.webfluxdemo.service;

import io.will.webfluxdemo.model.ExportResult;
import io.will.webfluxdemo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DataExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataExportService.class);
    
    // Simulated user database
    private final List<User> userDatabase = Arrays.asList(
        new User(1L, "Alice Johnson", "alice.johnson@example.com"),
        new User(2L, "Bob Smith", "bob.smith@example.com"),
        new User(3L, "Charlie Brown", "charlie.brown@example.com"),
        new User(4L, "Diana Prince", "diana.prince@example.com"),
        new User(5L, "Edward Wilson", "edward.wilson@example.com"),
        new User(6L, "Fiona Davis", "fiona.davis@example.com"),
        new User(7L, "George Miller", "george.miller@example.com"),
        new User(8L, "Helen Taylor", "helen.taylor@example.com"),
        new User(9L, "Ivan Rodriguez", "ivan.rodriguez@example.com"),
        new User(10L, "Julia Anderson", "julia.anderson@example.com")
    );

    /**
     * Simulates a long-running async export operation in a blocking way.
     * This is where the real work happens - reading from database, 
     * processing data, generating files, etc.
     * Fits for traditional data operations.
     */
    public Mono<ExportResult> exportAsync(Long requestId) {
        logger.info("Starting async export for request ID: {}", requestId);
        
        return Mono.fromCallable(() -> {
            // Simulate data processing steps
            logger.info("Step 1: Querying database for export data...");
            
            // Simulate database query time
            try {
                Thread.sleep(2000); // 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Export interrupted", e);
            }
            
            logger.info("Step 2: Processing {} records...", userDatabase.size());
            
            // Simulate data processing time
            try {
                Thread.sleep(3000); // 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Export interrupted", e);
            }
            
            logger.info("Step 3: Generating export file...");
            
            // Simulate file generation time
            try {
                Thread.sleep(2000); // 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Export interrupted", e);
            }
            
            // Simulate occasional failures for demonstration
            if (ThreadLocalRandom.current().nextInt(100) < 10) { // 10% failure rate
                throw new RuntimeException("Export failed due to system error");
            }
            
            // Generate result
            String fileName = String.format("user_export_%d_%d.csv", 
                requestId, System.currentTimeMillis());
            String downloadUrl = String.format("/api/downloads/%s", fileName);
            long fileSize = userDatabase.size() * 50L; // Simulate file size
            
            logger.info("Export completed successfully for request ID: {}", requestId);
            
            return new ExportResult(
                requestId,
                fileName,
                downloadUrl,
                (long) userDatabase.size(),
                "COMPLETED",
                LocalDateTime.now(),
                fileSize
            );
        })
        .subscribeOn(Schedulers.boundedElastic()) // Use bounded elastic scheduler for blocking operations
        .doOnError(error -> logger.error("Export failed for request ID: {}", requestId, error))
        .onErrorMap(throwable -> new RuntimeException("Export operation failed: " + throwable.getMessage(), throwable));
    }

    /**
     * Alternative implementation showing reactive processing.
     * This version processes data in a more reactive way.
     * Recommended.
     */
    public Mono<ExportResult> exportAsyncReactive(Long requestId) {
        logger.info("Starting reactive export for request ID: {}", requestId);
        
        return Mono.just(requestId)
            .doOnNext(id -> logger.info("Starting export process for ID: {}", id))
            
            // Step 1: Simulate database query with delay
            .delayElement(Duration.ofSeconds(2))
            .doOnNext(id -> logger.info("Database query completed for ID: {}", id))
            
            // Step 2: Simulate data processing
            .delayElement(Duration.ofSeconds(3))
            .doOnNext(id -> logger.info("Data processing completed for ID: {}", id))
            
            // Step 3: Simulate file generation
            .delayElement(Duration.ofSeconds(2))
            .doOnNext(id -> logger.info("File generation completed for ID: {}", id))
            
            // Generate final result
            .map(id -> {
                String fileName = String.format("user_export_reactive_%d_%d.csv", 
                    id, System.currentTimeMillis());
                String downloadUrl = String.format("/api/downloads/%s", fileName);
                long fileSize = userDatabase.size() * 50L;
                
                return new ExportResult(
                    id,
                    fileName,
                    downloadUrl,
                    (long) userDatabase.size(),
                    "COMPLETED",
                    LocalDateTime.now(),
                    fileSize
                );
            })
            .doOnSuccess(result -> logger.info("Reactive export completed: {}", result))
            .doOnError(error -> logger.error("Reactive export failed for request ID: {}", requestId, error));
    }

    /**
     * Simulates a batch processing operation with progress updates.
     * Returns intermediate results for demonstration.
     * Fits for large amount of data.
     */
    public Mono<ExportResult> exportWithProgress(Long requestId) {
        logger.info("Starting batch export with progress for request ID: {}", requestId);
        
        return Mono.fromCallable(() -> {
            int totalBatches = 5;
            
            for (int i = 1; i <= totalBatches; i++) {
                logger.info("Processing batch {}/{} for request ID: {}", i, totalBatches, requestId);
                
                try {
                    Thread.sleep(1000); // 1 second per batch
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Batch processing interrupted", e);
                }
            }
            
            String fileName = String.format("batch_export_%d_%d.csv", 
                requestId, System.currentTimeMillis());
            String downloadUrl = String.format("/api/downloads/%s", fileName);
            
            return new ExportResult(
                requestId,
                fileName,
                downloadUrl,
                (long) userDatabase.size(),
                "COMPLETED",
                LocalDateTime.now(),
                userDatabase.size() * 75L
            );
        })
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofMinutes(2)) // Set timeout for long operations
        .doOnError(error -> logger.error("Batch export failed for request ID: {}", requestId, error));
    }
}