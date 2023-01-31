package org.flyboy.bells.security.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;

/**
 * Rest client to remote token endpoints to exchange and refresh tokens.
 * @author John J. Franey
 */
@RegisterRestClient(configKey = "token-service")
public interface AuthToken {
    // query/body values

    // code (from authorization server)
    // grant_type: authorization_code)
    // code_verifier (pkce)

    public static record Code(
            String code,
            @JsonProperty("grant_type") String grantType,
            @JsonProperty("code_verifier") String codeVerifier
    ){};

    public static record Tokens(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Integer expiresIn,
            @JsonProperty("refresh_token") String refreshToken
    ){};

    @POST
    Uni<Tokens> exchange(Code request);

    @POST
    Uni<Tokens> exchange(
            @QueryParam("client_id") String clientId,
            @QueryParam("client_secret") String clientSecret,
            @QueryParam("code") String code,
            @QueryParam("code_verifier") String codeVerifier,
            @QueryParam("redirect_uri") String redirectUri,
            @QueryParam("grant_type") String grantType);

}

