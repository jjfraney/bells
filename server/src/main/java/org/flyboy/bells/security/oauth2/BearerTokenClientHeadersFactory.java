package org.flyboy.bells.security.oauth2;

import io.quarkus.rest.client.reactive.ReactiveClientHeadersFactory;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Reactive client headers factory to add oauth token to client headers.
 * @author John J. Franey
 */
@ApplicationScoped
public class BearerTokenClientHeadersFactory extends ReactiveClientHeadersFactory {
    @Inject
    TokenService tokenService;


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
