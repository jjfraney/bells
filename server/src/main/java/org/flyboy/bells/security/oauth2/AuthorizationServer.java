package org.flyboy.bells.security.oauth2;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.QueryParam;

/**
 * Rest client to remote token endpoints to exchange and refresh tokens.
 * @author John J. Franey
 */
@RegisterRestClient(configKey = "token-service")
@RegisterProvider(AuthorizationExceptionMapper.class)
public interface AuthorizationServer {
    /**
     * sends code exchange request to token service.
     * @param clientId - OAuth2 client id
     * @param clientSecret- OAuth2 client secret
     * @param code - OAuth2 code obtained from OAuth2 authentication request
     * @param codeVerifier - verifier component of Proof Key for Code Exchange
     * @param redirectUri - which received the OAuth2 authentication response
     * @param grantType - authorization_code
     * @return authorization response
     * @throws AuthorizationException in case token service return 401 status.
     */
    @POST
    Uni<AuthorizationResponse> exchange(
            @QueryParam("client_id") String clientId,
            @QueryParam("client_secret") String clientSecret,
            @QueryParam("code") String code,
            @QueryParam("code_verifier") String codeVerifier,
            @QueryParam("redirect_uri") String redirectUri,
            @QueryParam("grant_type") String grantType
    );


    /**
     * sends refresh token request to token service
     * @param clientId - OAuth2 client id
     * @param clientSecret- OAuth2 client secret
     * @param grantType - refresh_token
     * @param refreshToken - obtained from OAuth token service during code exchange
     * @param scope - should be same as used in original OAuth2 authentication request
     * @return authorization response
     * @throws AuthorizationException in case token service returns 401 status.
     */
    @POST
    Uni<AuthorizationResponse> refresh(
            @QueryParam("client_id") String clientId,
            @QueryParam("client_secret") String clientSecret,
            @QueryParam("grant_type") String grantType,
            @QueryParam("refresh_token") String refreshToken,
            @QueryParam("scope") String scope

    );

}


