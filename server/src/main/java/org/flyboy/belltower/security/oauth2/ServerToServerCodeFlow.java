package org.flyboy.belltower.security.oauth2;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author John J. Franey
 */
@RequestScoped
public class ServerToServerCodeFlow {
    private final Logger logger = LoggerFactory.getLogger(ServerToServerCodeFlow.class);

    @Inject
    CodeCallbackEndpoint callbackEndpoint;


    @RestClient
    AuthorizationServer authToken;

    @ConfigProperty(name="calendar-scopes")
    String calendarScopes;

    @ConfigProperty(name="client-id")
    String clientId;

    @ConfigProperty(name="client-secret")
    String clientSecret;

    @ConfigProperty(name="auth-service-url")
    String authServiceUrl;

    private final ProofKey pkce = ProofKey.generate();

    public Uni<Tokens> getToken() {

        // enable the callback endpoint
        return callbackEndpoint.enable()

                // visit the auth url in the browser, which redirects to our callbackEndpoint
                .onItem().invoke(this::browseAuthUrl)

                // when the callbackEndpoint gets code, emit it downstream
                .onItem().transformToUni(h -> callbackEndpoint.createCodeEmitter())


                // exchange the code for tokens: access and refresh
                .onItem().transformToUni(this::exchange)

                .onItem().transform(t -> new Tokens(
                    t.accessToken(),
                    t.refreshToken(),
                    ZonedDateTime.now().plusSeconds(t.expiresIn()),
                    t.scope().orElse(calendarScopes)))
                ;
    }

    public Uni<Tokens> refresh(Tokens tokens) {
        final String grantType = "refresh_token";
        final String scope = tokens.scope();
        final String refreshToken = tokens.refreshToken();
        return authToken
                .refresh(clientId, clientSecret, grantType, refreshToken, scope)

                .onFailure(ProcessingException.class).transform(Throwable::getCause)
                .onFailure(AuthorizationException.class)
                .invoke(e -> logger.error("during refresh: {}", e.getMessage()))

                .onItem().transform(t -> {
                    // response does not contain the refresh token
                    return new Tokens(
                            t.accessToken(),
                            refreshToken,
                            ZonedDateTime.now().plusSeconds(t.expiresIn()),
                            t.scope().orElse(calendarScopes));
                });
    }

    private Uni<AuthorizationResponse> exchange(String code) {
        final String grantType = "authorization_code";
        final String codeVerifier = pkce.verifier();
        final String redirectUri = callbackEndpoint.getUri();
        return authToken.exchange(
                clientId,
                clientSecret,
                code,
                codeVerifier,
                redirectUri,
                grantType
                )

                .onFailure(ProcessingException.class).transform(Throwable::getCause)
                .onFailure(AuthorizationException.class).invoke(e -> logger.error("during exchange: {}", e.getMessage()))
                ;
    }

    private void browseAuthUrl(CodeCallbackEndpoint.Info callbackInfo) {
        // for now, just get the uri and log it.
        // later: use desktop browser to browse the auth url

        String queryParams = Map.of(
                "state", callbackInfo.state(),
                "redirect_uri", callbackInfo.redirectUri(),
                "response_type", "code",
                "scope", calendarScopes,
                "client_id", clientId,
                "code_challenge", pkce.challenge(),
                "code_challenge_method", pkce.method()
        )
                .entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        URI uri = URI.create(authServiceUrl + "?" + queryParams);

        logger.info("auth uri: {}", uri);

    }
}
