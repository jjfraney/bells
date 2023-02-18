package org.flyboy.bells.security.oauth2;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author John J. Franey
 */
class AuthorizationExceptionMapper implements ResponseExceptionMapper<AuthorizationException> {

    @Override
    public AuthorizationException toThrowable(Response response) {

        final AuthorizationErrorResponse errorResponse = response.readEntity(AuthorizationErrorResponse.class);
        // make sure we throw some value for the error
        final AuthorizationErrorResponse result = errorResponse.error() == null
                ? new AuthorizationErrorResponse("invalid_request", errorResponse.errorDescription(), errorResponse.errorUri())
                : errorResponse;

        throw new AuthorizationException(result);
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status == 400;
    }
}
