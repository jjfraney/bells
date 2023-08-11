package org.flyboy.belltower.security.oauth2;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.common.util.QuarkusMultivaluedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * @author John J. Franey
 */
public class AuthorizationExceptionMapperTest {

    private AuthorizationExceptionMapper mapper;

    @BeforeEach
    public void instantiate() {
        mapper = new AuthorizationExceptionMapper();
    }
    @Test
    public void testHandles() {
        MultivaluedMap<String, Object> empty = new QuarkusMultivaluedHashMap<>()
                ;
        Assertions.assertTrue(mapper.handles(400, empty));
    }

    @Test
    public void testThrowsAuthorizationException() {
        AuthorizationErrorResponse errorResponse = new AuthorizationErrorResponse("error",
                Optional.empty(), null);

        Response response = Mockito.mock(Response.class);
        Mockito.when(response.readEntity(AuthorizationErrorResponse.class)).thenReturn(errorResponse);

        AuthorizationException exception =
                Assertions.assertThrows(AuthorizationException.class, () -> mapper.toThrowable(response));
        Assertions.assertEquals(errorResponse.error(), exception.getError());

    }

    @Test
    public void testThrowsAuthorizationErrorResponse() {
        AuthorizationErrorResponse errorResponse = new AuthorizationErrorResponse(null,
                Optional.empty(), null);

        Response response = Mockito.mock(Response.class);
        Mockito.when(response.readEntity(AuthorizationErrorResponse.class)).thenReturn(errorResponse);

        AuthorizationException exception = Assertions.assertThrows(
                AuthorizationException.class, () -> mapper.toThrowable(response));
        Assertions.assertEquals("invalid_request", exception.getError());

    }
}
