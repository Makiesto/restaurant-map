package com.example.demo.config;

import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()

                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET, "/api/restaurants").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/*/menu").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/*/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/*/reviews/stats").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/dishes/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/allergens").permitAll()

                        // Authenticated "me" endpoints — before wildcard numeric ID rules
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/*/reviews/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/my").authenticated()
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/users/me/**").authenticated()

                        // Admin only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("/api/users/\\d+", "DELETE")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("/api/users/\\d+", "GET")).hasRole("ADMIN")

                        // Verified users + admins
                        .requestMatchers(HttpMethod.POST, "/api/restaurants").hasAnyRole("VERIFIED_USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/restaurants/*").hasAnyRole("VERIFIED_USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/restaurants/*").hasAnyRole("VERIFIED_USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/restaurants/*/dishes").hasAnyRole("VERIFIED_USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/dishes/*").hasAnyRole("VERIFIED_USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/dishes/*").hasAnyRole("VERIFIED_USER", "ADMIN")

                        // Authenticated
                        .requestMatchers(HttpMethod.POST, "/api/restaurants/*/reviews").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/*").authenticated()
                        .requestMatchers("/api/upload/**").authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Add exception handling to properly convert AccessDeniedException to 403
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Access Denied\",\"message\":\"" +
                                    accessDeniedException.getMessage() + "\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}