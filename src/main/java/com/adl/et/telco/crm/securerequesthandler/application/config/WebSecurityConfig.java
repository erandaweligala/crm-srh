package com.adl.et.telco.crm.securerequesthandler.application.config;

import com.adl.et.telco.crm.securerequesthandler.application.filter.JwtRequestFilter;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.UserDetailsServiceImplementation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class that sets up Spring Security with JWT authentication.
 * Configures authentication manager, password encoder, and HTTP security settings.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {
    private static final String LOG_PREFIX = "SRH|WebSecurityConfig|";

    private final UserDetailsServiceImplementation userDetailsServiceImplementation;
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("{}securityFilterChain|Start", LOG_PREFIX);
        
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/srh/auth/azure-ad-auth/saml2-request").permitAll()
                .requestMatchers("/srh/auth/azure-ad-auth/saml-res").permitAll()
                .requestMatchers("/srh/auth/user/login").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", 
                    "/v2/api-docs", "/webjars/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("{}securityFilterChain|End|HTTP Security configured with JWT authentication", LOG_PREFIX);
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