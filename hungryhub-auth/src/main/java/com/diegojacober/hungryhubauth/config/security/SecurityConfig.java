package com.diegojacober.hungryhubauth.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwk.JwkProvider;
import com.diegojacober.hungryhubauth.config.security.AccessToken.AccessTokenAuthenticationFailureHandler;
import com.diegojacober.hungryhubauth.config.security.AccessToken.AccessTokenFilter;
import com.diegojacober.hungryhubauth.facades.providers.EntraIdJwkProvider;
import com.diegojacober.hungryhubauth.facades.providers.KeycloakAuthenticationProvider;
import com.diegojacober.hungryhubauth.facades.providers.KeycloakJwkProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@Order(1)
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.ignored}")
    private String nonSecureUrl;

    @Value("${keycloak.jwk}")
    private String jwkProviderUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            final AuthenticationManagerBuilder auth,
            final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors((cors) -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults())
                .addFilterBefore(
                        new AccessTokenFilter(
                                jwtTokenValidator(entradIdJwkProvider()),
                                authenticationManager(authenticationConfiguration),
                                authenticationFailureHandler()),
                        BasicAuthenticationFilter.class);
        ;
        return http.build();
    }
    
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new AccessTokenAuthenticationFailureHandler();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AuthorizationAccessDeniedHandler();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(nonSecureUrl);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new KeycloakAuthenticationProvider();
    }

    @Bean
    public JwtTokenValidator jwtTokenValidator(JwkProvider jwkProvider) {
        return new JwtTokenValidator(jwkProvider);
    }

    @Bean
    public JwkProvider keycloakJwkProvider() {
        return new KeycloakJwkProvider(jwkProviderUrl);
    }

    @Bean
    public JwkProvider entradIdJwkProvider() {
        return new EntraIdJwkProvider(jwkProviderUrl);
    }
}
