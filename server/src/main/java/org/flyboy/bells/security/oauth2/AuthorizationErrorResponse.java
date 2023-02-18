package org.flyboy.bells.security.oauth2;

import java.util.Optional;

/**
 * Content of authorization error response per RFC 6749:
 * <pre>
 *    error
 *          invalid_request
 *                The request is missing a required parameter, includes an
 *                unsupported parameter value (other than grant type),
 *                repeats a parameter, includes multiple credentials,
 *                utilizes more than one mechanism for authenticating the
 *                client, or is otherwise malformed.
 *
 *          invalid_client
 *                Client authentication failed (e.g., unknown client, no
 *                client authentication included, or unsupported
 *                authentication method).  The authorization server MAY
 *                return an HTTP 401 (Unauthorized) status code to indicate
 *                which HTTP authentication schemes are supported.  If the
 *                client attempted to authenticate via the "Authorization"
 *                request header field, the authorization server MUST
 *                respond with an HTTP 401 (Unauthorized) status code and
 *                include the "WWW-Authenticate" response header field
 *                matching the authentication scheme used by the client.
 *
 *          invalid_grant
 *                The provided authorization grant (e.g., authorization
 *                code, resource owner credentials) or refresh token is
 *                invalid, expired, revoked, does not match the redirection
 *                URI used in the authorization request, or was issued to
 *                another client.
 *
 *          unauthorized_client
 *                The authenticated client is not authorized to use this
 *                authorization grant type.
 *
 *          unsupported_grant_type
 *                The authorization grant type is not supported by the
 *                authorization server.
 *
 *          invalid_scope
 *                The requested scope is invalid, unknown, malformed, or
 *                exceeds the scope granted by the resource owner.
 *
 *          Values for the "error" parameter MUST NOT include characters
 *          outside the set %x20-21 / %x23-5B / %x5D-7E.
 *
 *    error_description
 *          OPTIONAL.  Human-readable ASCII [USASCII] text providing
 *          additional information, used to assist the client developer in
 *          understanding the error that occurred.
 *          Values for the "error_description" parameter MUST NOT include
 *          characters outside the set %x20-21 / %x23-5B / %x5D-7E.
 *
 *    error_uri
 *          OPTIONAL.  A URI identifying a human-readable web page with
 *          information about the error, used to provide the client
 *          developer with additional information about the error.
 *          Values for the "error_uri" parameter MUST conform to the
 *          URI-reference syntax and thus MUST NOT include characters
 *          outside the set %x21 / %x23-5B / %x5D-7E.
 * </pre>
 * @author John J. Franey
 */
record AuthorizationErrorResponse(String error, Optional<String> errorDescription, Optional<String> errorUri) {
    public AuthorizationErrorResponse(String error) {
        this(error, Optional.ofNullable(null), Optional.ofNullable(null));
    }
}
