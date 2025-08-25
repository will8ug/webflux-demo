# Spring Security Guide for WebFlux Demo

This guide explains the Spring Security features that have been added to the WebFlux demo project.

## Overview

The project now includes Spring Security with WebFlux support, providing:
- Authentication with in-memory users
- Role-based authorization
- Protected and public endpoints
- Security testing capabilities

## Configuration

### Security Configuration (`SecurityConfig.java`)

The security configuration is located in `src/main/java/io/will/webfluxdemo/config/SecurityConfig.java` and includes:

- **In-memory user details service** with two users:
  - `user` (password: `password`) with `USER` role
  - `admin` (password: `admin`) with `USER` and `ADMIN` roles

- **Authorization rules**:
  - `/api/auth/public` - Public access (no authentication required)
  - `/api/users` - Requires `USER` role
  - `/api/users/{id}` - Requires `USER` role
  - `/api/users/test-error` - Requires `ADMIN` role
  - `/api/users/test-bad-request` - Requires `ADMIN` role
  - `/api/auth/protected` - Requires `USER` role
  - `/api/auth/admin` - Requires `ADMIN` role
  - `/api/auth/me` - Requires `USER` role

## Available Endpoints

### Public Endpoints
- `GET /api/auth/public` - No authentication required

### Protected Endpoints (require authentication)
- `GET /api/auth/me` - Get current user information (USER role)
- `GET /api/auth/protected` - Protected endpoint (USER role)
- `GET /api/auth/admin` - Admin-only endpoint (ADMIN role)
- `GET /api/users` - Get all users (USER role)
- `GET /api/users/{id}` - Get user by ID (USER role)
- `POST /api/users` - Create user (USER role)
- `DELETE /api/users/{id}` - Delete user (USER role)

### Admin-Only Endpoints
- `GET /api/users/test-error` - Test error endpoint (ADMIN role)
- `GET /api/users/test-bad-request` - Test bad request endpoint (ADMIN role)

## Testing

### Running Tests
```bash
mvn test
```

### Manual Testing with curl

1. **Public endpoint** (no authentication):
```bash
curl http://localhost:8080/api/auth/public
```

2. **Protected endpoint** (will return 401 Unauthorized):
```bash
curl http://localhost:8080/api/auth/protected
```

3. **Authenticated request** (using HTTP Basic Auth):
```bash
# With user credentials
curl -u user:password http://localhost:8080/api/auth/protected

# With admin credentials
curl -u admin:admin http://localhost:8080/api/auth/admin
```

4. **Get current user info**:
```bash
curl -u user:password http://localhost:8080/api/auth/me
```

## Security Features

### Authentication
- In-memory user authentication
- BCrypt password encoding
- HTTP Basic authentication (disabled by default, but can be enabled)

### Authorization
- Role-based access control
- Path-based security rules
- Method-level security support

### Security Headers
- CSRF protection (disabled for API endpoints)
- Security headers automatically applied

## Customization

To customize the security configuration:

1. **Add more users**: Modify the `userDetailsService()` method in `SecurityConfig.java`
2. **Change authorization rules**: Update the `authorizeExchange()` configuration
3. **Enable HTTP Basic Auth**: Remove the `.httpBasic(httpBasic -> httpBasic.disable())` line
4. **Enable form login**: Remove the `.formLogin(formLogin -> formLogin.disable())` line

## Dependencies

The following Spring Security dependencies have been added:
- `spring-boot-starter-security` - Core security functionality
- `spring-security-test` - Testing support (test scope)

## Notes

- The current configuration disables HTTP Basic authentication and form login for API endpoints
- CSRF protection is disabled for API endpoints
- All passwords are BCrypt encoded for security
- The security configuration is reactive and compatible with WebFlux
