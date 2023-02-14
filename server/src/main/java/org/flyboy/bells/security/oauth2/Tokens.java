package org.flyboy.bells.security.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author John J. Franey
 */
public record Tokens(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Integer expiresIn,
        @JsonProperty("refresh_token") String refreshToken
) {
}
