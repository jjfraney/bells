package org.flyboy.bells.security.oauth2;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;

/**
 * @author John J. Franey
 */
public class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    public void beforeEach() {
        tokenService = new TokenService();
        tokenService.tokenStore = Mockito.mock(TokenStore.class);
        tokenService.serverToServerCodeFlow = Mockito.mock(ServerToServerCodeFlow.class);
    }

    @Test
    public void testTokenStoreStillValidToken() {
        Tokens expectedTokens = new Tokens("xxxxx", "yyyyy", ZonedDateTime.now().plusMinutes(2), "abc");

        Mockito.when(tokenService.tokenStore.read()).thenReturn(expectedTokens);
        Uni<Tokens> uni = tokenService.tokensFromStore();
        UniAssertSubscriber<Tokens> subscriber = uni
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertCompleted().assertItem(expectedTokens);
    }

    @Test
    public void testTokenStoreExpiredToken() {
        Tokens storedTokens = new Tokens("wwwwwww", "uuuuuuu", ZonedDateTime.now().minusMinutes(1), "abc");

        Mockito.when(tokenService.tokenStore.read()).thenReturn(storedTokens);

        Tokens expectedTokens = new Tokens("xxxxx", "yyyyy", ZonedDateTime.now().plusMinutes(2), "abc");
        Mockito.when(tokenService.serverToServerCodeFlow.refresh(storedTokens)).thenReturn(Uni.createFrom().item(expectedTokens));

        Uni<Tokens> uni = tokenService.tokensFromStore();
        UniAssertSubscriber<Tokens> subscriber = uni
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertCompleted().assertItem(expectedTokens);
    }
}
