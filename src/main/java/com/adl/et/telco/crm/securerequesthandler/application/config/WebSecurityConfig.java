package com.adl.et.telco.crm.securerequesthandler.application.config;

import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.UserDetailsServiceImplementation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class.
 *
 * AUTHENTICATION BYPASS: every endpoint is public. No JWT filter is registered,
 * no token is validated and no authority is required, so the Route Controllers
 * forward every request straight through to the downstream microservices.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {
    private static final String LOG_PREFIX = "SRH|WebSecurityConfig|";

    private final UserDetailsServiceImplementation userDetailsServiceImplementation;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("{}securityFilterChain|Start", LOG_PREFIX);

        http
            .csrf(csrf -> csrf.disable())
            // Every request is permitted - authentication and authorization are switched off.
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // No login challenge of any kind, so an unauthenticated call is never rejected.
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout -> logout.disable())
            .anonymous(anonymous -> anonymous.disable());

        log.warn("{}securityFilterChain|End|AUTHENTICATION BYPASS ACTIVE - all endpoints are public", LOG_PREFIX);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.debug("{}authenticationProvider|Start", LOG_PREFIX);
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsServiceImplementation);
        authProvider.setPasswordEncoder(passwordEncoder());
        log.info("{}authenticationProvider|End|AuthenticationProvider configured", LOG_PREFIX);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.debug("{}authenticationManager|Start", LOG_PREFIX);
        AuthenticationManager manager = config.getAuthenticationManager();
        log.info("{}authenticationManager|End|AuthenticationManager created", LOG_PREFIX);
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("{}passwordEncoder|Start", LOG_PREFIX);
        PasswordEncoder encoder = NoOpPasswordEncoder.getInstance();
        log.info("{}passwordEncoder|End|Using NoOpPasswordEncoder", LOG_PREFIX);
        return encoder;
    }
}
