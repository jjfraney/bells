package org.flyboy.belltower.security.oauth2;

import io.vertx.mutiny.core.http.HttpServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author John J. Franey
 */
public class CodeCallbackEndpointTest {

    CodeCallbackEndpoint callbackEndpoint;

    @BeforeEach
    public void beforeEach() {
        callbackEndpoint = new CodeCallbackEndpoint();
    }

    @Test
    public void testStateMatchesCodeNotEmpty() {
        // state matches expected state
        String expectedState = "xxxxx";
        String actualState = "xxxxx";
        String actualCode = "yyyyy";
        boolean actual = callbackEndpoint.isValidCode(expectedState, actualState, actualCode);
        Assertions.assertTrue(actual);
    }
    @Test
    public void testStateDoesNotMatchCodeNotEmpty() {
        // state matches expected state
        String expectedState = "zzzzz";
        String actualState = "xxxxx";
        String actualCode = "yyyyy";
        boolean actual = callbackEndpoint.isValidCode(expectedState, actualState, actualCode);
        Assertions.assertFalse(actual);
    }

    @Test
    public void testStateMatchesCodeEmpty() {
        // state matches expected state
        String expectedState = "xxxxx";
        String actualState = "xxxxx";
        String actualCode = "";
        boolean actual = callbackEndpoint.isValidCode(expectedState, actualState, actualCode);
        Assertions.assertFalse(actual);
    }

    @Test
    public void testStateNullCodeNotEmpty() {
        // state matches expected state
        String expectedState = "xxxxx";
        String actualState = null;
        String actualCode = "yyyyy";
        boolean actual = callbackEndpoint.isValidCode(expectedState, actualState, actualCode);
        Assertions.assertFalse(actual);
    }

    @Test
    public void testStateMatchesCodeNull() {
        // state matches expected state
        String expectedState = "xxxxx";
        String actualState = "xxxxx";
        String actualCode = null;
        boolean actual = callbackEndpoint.isValidCode(expectedState, actualState, actualCode);
        Assertions.assertFalse(actual);
    }

    @Test
    public void testUniqueState() {
        callbackEndpoint.stateLength = 30;
        Set<String> states =  IntStream.range(0,70)
                .mapToObj(i -> callbackEndpoint.generateState())
                .collect(Collectors.toSet());
        Assertions.assertEquals(70, states.size());
    }

    @Test
    public void testMakeUri() {
        callbackEndpoint.path = "test/path";
        callbackEndpoint.httpServer = Mockito.mock(HttpServer.class);

        Integer expectedPort = 106282;
        Mockito.when(callbackEndpoint.httpServer.actualPort()).thenReturn(expectedPort);

        String uri = callbackEndpoint.getUri();
        String expected = "http://localhost:" + expectedPort + "/" + callbackEndpoint.path;
        Assertions.assertEquals(expected, uri);
    }
}
