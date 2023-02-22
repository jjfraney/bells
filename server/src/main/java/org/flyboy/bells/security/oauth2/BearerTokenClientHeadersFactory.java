package org.flyboy.bells.security.oauth2;

import io.quarkus.rest.client.reactive.ReactiveClientHeadersFactory;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Reactive client headers factory to add oauth token to client headers.
 * @author John J. Franey
 */
@ApplicationScoped
public class BearerTokenClientHeadersFactory extends ReactiveClientHeadersFactory {
    @Inject
    TokenService tokenService;


    /**
     * returns headers for google calendar api
     * @param incomingHeaders
     * @param clientOutgoingHeaders
     * @return
     */

    @Override
    public Uni<MultivaluedMap<String, String>> getHeaders(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {

        return tokenService.getToken()
                .onItem().transform(token -> {
                    MultivaluedHashMap<String, String> newHeaders = new MultivaluedHashMap<>();
                    // perform blocking call
                    newHeaders.add("Authorization", "Bearer " + token);
                    return newHeaders;
                });
    }
}
