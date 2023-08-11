package org.flyboy.belltower.security.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

/**
 * @author John J. Franey
 */
public record Tokens(
        @JsonProperty String accessToken,
        @JsonProperty String refreshToken,
        @JsonProperty ZonedDateTime expires,
        @JsonProperty String scope) {
}
