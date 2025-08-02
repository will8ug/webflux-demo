# WebFlux Error Handling & Logging Guide

## 🎯 Problem Solved

**Errors occurring but not being logged in the backend**. This guide provides a comprehensive solution for global error handling and logging in Spring WebFlux applications.

## 🏗️ Solution Architecture

### 1. **Global Exception Handler** (`GlobalExceptionHandler.java`)
- **Purpose**: Catches ALL unhandled exceptions globally
- **Priority**: High priority (-2) to intercept before default handlers
- **Features**:
  - Logs detailed error information with request context
  - Returns standardized JSON error responses
  - Includes request ID for correlation

### 2. **Custom Exception Handler** (`CustomExceptionHandler.java`)
- **Purpose**: Handles specific business exceptions with proper HTTP status codes
- **Features**:
  - Type-specific exception handling
  - Proper HTTP status codes (400, 500, etc.)
  - Detailed logging for each exception type

### 3. **Request/Response Logging** (`WebFluxConfig.java`)
- **Purpose**: Logs ALL requests and responses with timing
- **Features**:
  - `requestLoggingFilter()` WebFilter bean for request/response logging
  - `errorLoggingFilter()` WebFilter bean for error logging
  - Unique request ID generation
  - Request/response timing
  - Client information (IP, User-Agent)
  - Error correlation

### 4. **Enhanced Logging Configuration**
- **File logging**: All logs saved to `logs/webflux-demo.log`
- **Log rotation**: 10MB max size, 30 days retention
- **Structured logging**: Consistent format with timestamps

## 🚀 How It Works

### Request Flow:
```
1. Request arrives → WebFluxConfig.requestLoggingFilter() logs start
2. Request processed → Controller/Service logic
3. If error occurs → CustomExceptionHandler or GlobalExceptionHandler
4. Response sent → WebFluxConfig.requestLoggingFilter() logs end with timing
```

### WebFilter Beans in WebFluxConfig:
- **`requestLoggingFilter()`**: A `@Bean` method that returns a `WebFilter` for logging all requests/responses
- **`errorLoggingFilter()`**: A `@Bean` method that returns a `WebFilter` for logging errors
- These are not separate classes, but WebFilter implementations defined as beans

### Error Handling Hierarchy:
```
1. CustomExceptionHandler (specific exceptions)
2. GlobalExceptionHandler (all unhandled exceptions)
3. Default Spring error handling (fallback)
```

## 🧪 Testing the Solution

### Manual Test Commands:
```bash
# Test custom exception handler
curl http://localhost:9001/api/users/test-error

# Test bad request handler
curl http://localhost:9001/api/users/test-bad-request

# Test 404 (non-existent endpoint)
curl http://localhost:9001/nonexistent-endpoint
```

### Automated Integration Tests:
The above curl commands have been converted to automated integration tests in `src/it/java/io/will/webfluxdemo/integration/UserIntegrationTest.java`:

```java
@Test
@Tag("ErrorHandling")
void testError_ShouldReturn500WithCustomExceptionHandler() {
    // Tests /api/users/test-error endpoint
}

@Test
@Tag("ErrorHandling")
void testBadRequest_ShouldReturn400WithCustomExceptionHandler() {
    // Tests /api/users/test-bad-request endpoint
}

@Test
@Tag("ErrorHandling")
void nonexistentEndpoint_ShouldReturn404WithGlobalExceptionHandler() {
    // Tests /nonexistent-endpoint endpoint
}
```

**Run the tests:**
```bash
# Run only integration tests
./mvnw integration-test

# Run only error handling integration tests
./mvnw integration-test -Dgroups=ErrorHandling

# Run all tests (unit + integration)
./mvnw verify
```

### Expected Results:
1. **All requests logged** in `logs/webflux-demo.log`
2. **Error details captured** with stack traces
3. **Request IDs** for correlation
4. **Standardized JSON responses** for clients

## 📝 Log Examples

### Request Log (from WebFluxConfig.requestLoggingFilter()):
```
=== REQUEST START ===
Request ID: 89f82b0c-1
Path: GET /api/users/test-error
Remote Address: 127.0.0.1
User-Agent: curl/8.7.1
Content-Type: null
```

### Error Log:
```
=== RUNTIMEEXCEPTION HANDLED ===
Request ID: 89f82b0c-1
Path: GET /api/users/test-error
Error Type: RuntimeException
Error Message: This is a test error for demonstration
Stack Trace: java.lang.RuntimeException: This is a test error...
```

### Response Log (from WebFluxConfig.requestLoggingFilter()):
```
=== REQUEST END ===
Request ID: 89f82b0c-1
Status: 500
Duration: 15ms
==================
```

## 🔧 Production Recommendations

### 1. **Monitoring Integration**
```java
// Add metrics collection
@EventListener
public void handleError(ErrorEvent event) {
    // Send to monitoring system (Prometheus, etc.)
    meterRegistry.counter("errors", "type", event.getException().getClass().getSimpleName()).increment();
}
```

### 2. **Alerting**
```java
// Add alerting for critical errors
if (ex instanceof CriticalBusinessException) {
    alertingService.sendAlert("Critical error occurred", ex);
}
```

### 3. **Request Tracing**
```java
// Add distributed tracing (Jaeger, Zipkin)
@Bean
public WebFilter tracingFilter() {
    return (exchange, chain) -> {
        String traceId = generateTraceId();
        exchange.getAttributes().put("traceId", traceId);
        return chain.filter(exchange);
    };
}
```

## 🎯 Key Benefits

1. **🔍 Full Visibility**: Every request and error is logged
2. **🔗 Correlation**: Request IDs link client requests to server logs
3. **📊 Performance**: Response times are tracked
4. **🛡️ Resilience**: Graceful error handling with proper HTTP status codes
5. **📈 Monitoring**: Ready for production monitoring integration
6. **🔧 Debugging**: Detailed stack traces and context information

## 🚀 Next Steps

1. **Deploy and monitor** the application
2. **Set up log aggregation** (ELK stack, Splunk, etc.)
3. **Configure alerts** for error thresholds
4. **Add metrics collection** for business KPIs
5. **Implement distributed tracing** for microservices

## 📁 File Structure

```
src/main/java/io/will/webfluxdemo/
├── exception/
│   ├── GlobalExceptionHandler.java    # Global error handling
│   └── CustomExceptionHandler.java    # Specific exception handling
├── config/
│   └── WebFluxConfig.java             # Request/response logging
└── controller/
    └── UserController.java            # Example endpoints

src/main/resources/
└── application.properties             # Logging configuration

logs/
└── webflux-demo.log                   # Application logs
```

## 🔍 Troubleshooting

### Common Issues:

1. **Port already in use**: `pkill -f "spring-boot:run"`
2. **Logs not appearing**: Check `logs/webflux-demo.log` permissions
3. **Error handler not working**: Verify `@Component` annotations
4. **Request IDs missing**: Check WebFluxConfig bean creation

### Debug Commands:
```bash
# Check if application is running
curl http://localhost:9001/actuator/health

# Monitor logs in real-time
tail -f logs/webflux-demo.log

# Test error handling
curl -v http://localhost:9001/api/users/test-error
```

This solution ensures you'll never again have the frustrating situation where customers report errors but you can't find them in your logs!
