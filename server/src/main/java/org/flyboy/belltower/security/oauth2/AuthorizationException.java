package org.flyboy.belltower.security.oauth2;

/**
 *
 * @author John J. Franey
 */
public class AuthorizationException extends RuntimeException {

    private final AuthorizationErrorResponse response;

    public AuthorizationException(AuthorizationErrorResponse response) {
        this.response = response;
    }

    public String getError() {
        return response.error();
    }

    public String getDescription() {
        return response.errorDescription().orElse(rfcDescription());
    }

    private String rfcDescription() {
        return switch (getError()) {
            default -> "none";
            case "invalid_request" -> "The request is missing a required parameter, includes an " +
                    "unsupported parameter value (other than grant type), " +
                    "repeats a parameter, includes multiple credentials,  " +
                    "utilizes more than one mechanism for authenticating the " +
                    "client, or is otherwise malformed.";
            case "invalid_client" -> "Client authentication failed (e.g., unknown client, no " +
                    "client authentication included, or unsupported " +
                    "authentication method).";
            case "invalid_grant" -> "The provided authorization grant (e.g., authorization " +
                    "code, resource owner credentials) or refresh token is " +
                    "invalid, expired, revoked, does not match the redirection " +
                    "URI used in the authorization request, or was issued to " +
                    "another client.";
            case "unauthorized_client" -> "The authenticated client is not authorized to use this " +
                    "authorization grant type.";
            case "unsupported_grant_type" -> "The authorization grant type is not supported by the " +
                    "authorization server.";
            case "invalid_scope" -> "The requested scope is invalid, unknown, malformed, or " +
                    "exceeds the scope granted by the resource owner.";
        };
    }

    @Override
    public String getMessage() {
        return "authorization token error: " + getError() + ", description: " + getDescription();
    }


}
