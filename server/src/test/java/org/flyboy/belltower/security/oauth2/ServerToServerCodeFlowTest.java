package org.flyboy.belltower.security.oauth2;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author John J. Franey
 */
public class ServerToServerCodeFlowTest {

    ServerToServerCodeFlow serverToServerCodeFlow;

    @BeforeEach
    public void beforeEach() {
        serverToServerCodeFlow = new ServerToServerCodeFlow();
        serverToServerCodeFlow.clientId = "someClientId";
        serverToServerCodeFlow.calendarScopes = "someCalendarScopes";
        serverToServerCodeFlow.clientSecret = "someClientSecret";
        serverToServerCodeFlow.authServiceUrl = "someServiceUrl";

        serverToServerCodeFlow.callbackEndpoint = Mockito.mock(CodeCallbackEndpoint.class);
        serverToServerCodeFlow.authToken = Mockito.mock(AuthorizationServer.class);
    }

    @Test
    public void testGetToken() {
        CodeCallbackEndpoint.Info info = new CodeCallbackEndpoint.Info("someRedirectUri", "someRandomState");
        Mockito.when(serverToServerCodeFlow.callbackEndpoint.enable()).thenReturn(Uni.createFrom().item(info));

        Uni<String> emitter = Uni.createFrom().emitter(e -> e.complete("someCode"));
        Mockito.when(serverToServerCodeFlow.callbackEndpoint.createCodeEmitter()).thenReturn(emitter);

        Mockito.when(serverToServerCodeFlow.callbackEndpoint.getUri()).thenReturn("someCallbackUri");

        AuthorizationResponse authorizationResponse =
                new AuthorizationResponse("myAccessToken",
                        "code",
                        999L,
                        "myRefreshTOken",
                        Optional.of("myScope"));
        Mockito
                .when(serverToServerCodeFlow.authToken.exchange(
                        any(String.class),
                        any(String.class),
                        any(String.class),
                        any(String.class),
                        any(String.class),
                        any(String.class)))
                .thenReturn(Uni.createFrom().item(authorizationResponse));

        Uni<Tokens> uni = serverToServerCodeFlow.getToken();

        UniAssertSubscriber<Tokens> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        Tokens result = subscriber.assertCompleted().awaitItem().getItem();

        Assertions.assertEquals("myAccessToken", result.accessToken());
    }

    @Test
    public void testRefresh() {
        Tokens refreshed = new Tokens("myAccessToken", "myRefreshToken", ZonedDateTime.now(), "myScope");
        AuthorizationResponse authorizationResponse =
                new AuthorizationResponse("myNewAccessToken",
                        "code",
                        999L,
                        null,
                        Optional.of("myScope"));

        Mockito.when(serverToServerCodeFlow.authToken.refresh(
                any(String.class), any(String.class), any(String.class), any(String.class), any(String.class))
        ).thenReturn(Uni.createFrom().item(authorizationResponse));

        Tokens tokens = new Tokens("myOldAccessToken", "myRefreshToken", ZonedDateTime.now(), "myScope");
        Uni<Tokens> uni = serverToServerCodeFlow.refresh(tokens);

        UniAssertSubscriber<Tokens> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        Tokens result = subscriber.assertCompleted().awaitItem().getItem();

        Assertions.assertEquals("myNewAccessToken", result.accessToken());
    }
}
