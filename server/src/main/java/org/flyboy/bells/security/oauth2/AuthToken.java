package org.flyboy.bells.security.oauth2;

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
    @POST
    Uni<Tokens> exchange(
            @QueryParam("client_id") String clientId,
            @QueryParam("client_secret") String clientSecret,
            @QueryParam("code") String code,
            @QueryParam("code_verifier") String codeVerifier,
            @QueryParam("redirect_uri") String redirectUri,
            @QueryParam("grant_type") String grantType);
}

