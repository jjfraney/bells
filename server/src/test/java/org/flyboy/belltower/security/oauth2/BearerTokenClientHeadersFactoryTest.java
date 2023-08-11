package org.flyboy.belltower.security.oauth2;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author John J. Franey
 */
public class BearerTokenClientHeadersFactoryTest {

    BearerTokenClientHeadersFactory factory;


    @BeforeEach
    public void beforeEach() {
        factory = new BearerTokenClientHeadersFactory();
        factory.tokenService = Mockito.mock(TokenService.class);
        Mockito.when(factory.tokenService.getToken()).thenReturn(Uni.createFrom().item("myTestToken"));
    }
    private final MultivaluedMap<String, String> EMPTY = new MultivaluedHashMap<>();

    @Test
    public void testToken() {
        Uni<MultivaluedMap<String, String>> uni = factory.getHeaders(EMPTY, EMPTY);

        UniAssertSubscriber<MultivaluedMap<String, String>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        MultivaluedMap<String, String> result = subscriber.assertCompleted().awaitItem().getItem();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Bearer myTestToken", result.get("Authorization").get(0));
    }
}
