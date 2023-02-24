package org.flyboy.bells.security.oauth2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * @author John J. Franey
 */
public class AuthorizationExceptionTest {
    AuthorizationException authorizationException;

    AuthorizationErrorResponse invalidRequest = new AuthorizationErrorResponse("invalid_request", Optional.ofNullable(null),Optional.ofNullable(null));
    AuthorizationErrorResponse invalidClient = new AuthorizationErrorResponse("invalid_client", Optional.ofNullable(null),Optional.ofNullable(null));
    AuthorizationErrorResponse invalidGrant = new AuthorizationErrorResponse("invalid_grant", Optional.ofNullable(null),Optional.ofNullable(null));
    AuthorizationErrorResponse unauthorizedClient = new AuthorizationErrorResponse("unauthorized_client", Optional.ofNullable(null),Optional.ofNullable(null));
    AuthorizationErrorResponse unsupportedGrantType = new AuthorizationErrorResponse("unsupported_grant_type", Optional.ofNullable(null),Optional.ofNullable(null));
    AuthorizationErrorResponse invalidScope = new AuthorizationErrorResponse("invalid_scope", Optional.ofNullable(null),Optional.ofNullable(null));

    AuthorizationErrorResponse whoknows = new AuthorizationErrorResponse("who_knows", Optional.ofNullable(null),Optional.ofNullable(null));

    @Test
    public void testInvalidRequest() {
        AuthorizationException exception = new AuthorizationException(invalidRequest);
        Assertions.assertNotEquals("none", exception.getDescription());
    }

    @Test
    public void testInvalidCient() {
        AuthorizationException exception = new AuthorizationException(invalidClient);
        Assertions.assertNotEquals("none", exception.getDescription());
    }

    @Test
    public void testInvalidGrant() {
        AuthorizationException exception = new AuthorizationException(invalidGrant);
        Assertions.assertNotEquals("none", exception.getDescription());
    }

    @Test
    public void testUnauthorizedClient() {
        AuthorizationException exception = new AuthorizationException(unauthorizedClient);
        Assertions.assertNotEquals("none", exception.getDescription());
    }
    @Test
    public void testUnsupportedGrantType() {
        AuthorizationException exception = new AuthorizationException(unsupportedGrantType);
        Assertions.assertNotEquals("none", exception.getDescription());
    }
    @Test
    public void testInvalidScope() {
        AuthorizationException exception = new AuthorizationException(invalidScope);
        Assertions.assertNotEquals("none", exception.getDescription());
    }

    @Test
    public void testWhoKnows() {
        AuthorizationException exception = new AuthorizationException(whoknows);
        Assertions.assertEquals("none", exception.getDescription());
    }
}
