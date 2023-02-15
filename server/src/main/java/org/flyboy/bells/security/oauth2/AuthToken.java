package org.flyboy.bells.security.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.util.Optional;

/**
 * Rest client to remote token endpoints to exchange and refresh tokens.
 * @author John J. Franey
 */
@RegisterRestClient(configKey = "token-service")

//@RegisterProvider(RequestFilter.class)
public interface AuthToken {
    @POST
    Uni<Tokens> exchange(
            @QueryParam("client_id") String clientId,
            @QueryParam("client_secret") String clientSecret,
            @QueryParam("code") String code,
            @QueryParam("code_verifier") String codeVerifier,
            @QueryParam("redirect_uri") String redirectUri,
            @QueryParam("grant_type") String grantType
    );


    @POST
    Uni<Tokens> refresh(
            @QueryParam("client_id") String clientId,
            @QueryParam("client_secret") String clientSecret,
            @QueryParam("grant_type") String grantType,
            @QueryParam("refresh_token") String refreshToken,
            @QueryParam("scope") String scope

    );

    /**
     * @author John J. Franey
     */
    public static record Tokens(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("scope") Optional<String> scope
    ) {
    }
}

class RequestFilter implements ClientRequestFilter {

    private Logger logger = LoggerFactory.getLogger(ClientRequestFilter.class);
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        logger.info("uri: {}", requestContext.getUri());
    }
}


