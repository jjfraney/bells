package org.flyboy.bells.security.oauth2;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.net.URI;
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
    AuthToken authToken;

    @Inject
    Browser browser;

    @ConfigProperty(name="calendar-scopes")
    String calendarScopes;

    @ConfigProperty(name="client-id")
    String clientId;

    @ConfigProperty(name="client-secret")
    String clientSecret;

    @ConfigProperty(name="auth-service-url")
    String authServiceUrl;

    private ProofKey pkce = ProofKey.generate();

    public Uni<AuthToken.Tokens> getToken() {

        // enable the callback endpoint
        return callbackEndpoint.enable()

                // visit the auth url in the browser, which redirects to our callbackEndpoint
                .onItem().invoke(this::browseAuthUrl)

                // when the callbackEndpoint gets code, emit it downstream
                .onItem().transformToUni(h -> callbackEndpoint.createCodeEmitter())


                // exchange the code for tokens: access and refresh
                .onItem().transformToUni(this::exchange)
                ;
    }

    private Uni<AuthToken.Tokens> exchange(String code) {
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
        );
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
        browser.browse(uri);

    }
}
