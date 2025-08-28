package io.will.webfluxdemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        if (!securityEnabled) {
            logger.info("Security is disabled as feature flag is off");

            return http
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
        }
        
        // Normal security configuration when enabled
        http
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints
                .pathMatchers("/api/auth/public").permitAll()
                // Protected endpoints - require authentication
                .pathMatchers("/api/users").hasRole("USER")
                .pathMatchers("/api/users/{id}").hasRole("USER")
                .pathMatchers("/api/users/test-error").hasRole("ADMIN")
                .pathMatchers("/api/users/test-bad-request").hasRole("ADMIN")
                .pathMatchers("/api/auth/protected").hasRole("USER")
                .pathMatchers("/api/auth/admin").hasRole("ADMIN")
                .pathMatchers("/api/auth/me").hasRole("USER")
                // Any other request requires authentication
                .anyExchange().authenticated()
            )
            .httpBasic(httpBasic -> {})
            .formLogin(formLogin -> formLogin.disable())
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        if (!securityEnabled) {
            // Return minimal user details service when security is disabled
            // Still need at least one user to avoid "users cannot be null or empty" error
            UserDetails dummyUser = User.builder()
                .username("dummy")
                .password(passwordEncoder().encode("dummy"))
                .roles("USER")
                .build();
            return new MapReactiveUserDetailsService(dummyUser);
        }
        
        UserDetails user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build();

        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin"))
            .roles("USER", "ADMIN")
            .build();

        return new MapReactiveUserDetailsService(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
