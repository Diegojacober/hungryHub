package com.diegojacober.hungryhubauth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

class KeycloakAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) {
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}
