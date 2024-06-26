package com.diegojacober.hungryhubauth.config.security.AccessToken;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import com.diegojacober.hungryhubauth.config.security.JwtAuthentication;
import com.diegojacober.hungryhubauth.config.security.JwtTokenValidator;
import com.diegojacober.hungryhubauth.exceptions.InvalidTokenException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessTokenFilter extends AbstractAuthenticationProcessingFilter {


   private final JwtTokenValidator tokenVerifier;
   

    public AccessTokenFilter(
            JwtTokenValidator jwtTokenValidator,
            AuthenticationManager authenticationManager, AuthenticationFailureHandler authenticationFailureHandler) {

        super(AnyRequestMatcher.INSTANCE);
        setAuthenticationManager(authenticationManager);
        this.tokenVerifier = jwtTokenValidator;
        setAuthenticationFailureHandler(authenticationFailureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) {

        log.info("Attempting to authenticate for a request {}", request.getRequestURI());

        String authorizationHeader = extractAuthorizationHeaderAsString(request);
        AccessToken accessToken = tokenVerifier.validateAuthorizationHeader(authorizationHeader);
        return this.getAuthenticationManager()
                            .authenticate(new JwtAuthentication(accessToken));
    } 

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        log.info("Successfully authentication for the request {}", request.getRequestURI());

        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

    private String extractAuthorizationHeaderAsString(HttpServletRequest request) throws InvalidTokenException {
        try {
            return request.getHeader("Authorization");
        } catch (Exception ex){
            throw new InvalidTokenException("There is no Authorization header in a request");
        }
    }
}