package com.diegojacober.hungryhubauth;

import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.diegojacober.hungryhubauth.exceptions.InvalidTokenException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static java.util.Objects.isNull;

import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessToken {

    public static final String BEARER = "Bearer ";

    private final String value;

    public String getValueAsString() {
        return value;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() throws InvalidTokenException {
        DecodedJWT decodedJWT = decodeToken(value);
        JsonObject payloadAsJson = decodeTokenPayloadToJsonObject(decodedJWT);

       return StreamSupport.stream(
                payloadAsJson.getAsJsonObject("realm_access").getAsJsonArray("roles").spliterator(), false)
                .map(JsonElement::getAsString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private DecodedJWT decodeToken(String value) throws InvalidTokenException {
        if (isNull(value)){
            throw new InvalidTokenException("Token has not been provided");
        }
        return JWT.decode(value);
    }

    private JsonObject decodeTokenPayloadToJsonObject(DecodedJWT decodedJWT) throws InvalidTokenException {
        try {
            String payloadAsString = decodedJWT.getPayload();
            return new Gson().fromJson(
                    new String(Base64.getDecoder().decode(payloadAsString), StandardCharsets.UTF_8),
                    JsonObject.class);
        }   catch (RuntimeException exception){
            throw new InvalidTokenException("Invalid JWT or JSON format of each of the jwt parts");
        }
    }
}
