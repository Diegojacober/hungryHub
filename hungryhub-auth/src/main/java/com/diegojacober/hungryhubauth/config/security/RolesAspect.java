package com.diegojacober.hungryhubauth.config.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.diegojacober.hungryhubauth.config.security.AccessToken.AllowedRoles;

@Aspect
@Component
public class RolesAspect {

    @Before("@annotation(com.diegojacober.hungryhubauth.AllowedRoles)")
    public void before(JoinPoint joinPoint) {

        String[] expectedRoles = ((MethodSignature) joinPoint.getSignature()).getMethod()
                .getAnnotation(AllowedRoles.class).value();

        Collection<? extends GrantedAuthority> grantedAuthorities = Optional
                .ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getAuthorities)
                .orElseThrow(() -> new AccessDeniedException("No authorities found"));

        List<String> roles = grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (!roles.containsAll(Arrays.asList(expectedRoles))) {
            throw new AccessDeniedException(
                    String.format("Unauthorized request. Expected to have %s roles, but have %s",
                            Arrays.asList(expectedRoles), roles));
        }
    }
}