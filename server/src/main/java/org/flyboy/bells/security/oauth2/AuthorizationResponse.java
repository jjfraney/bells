package org.flyboy.bells.security.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * @author John J. Franey
 */
public record AuthorizationResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("scope") Optional<String> scope
) {
}
