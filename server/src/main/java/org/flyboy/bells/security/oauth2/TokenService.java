package org.flyboy.bells.security.oauth2;

import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

/**
 * Obtains an access token for authentication/authorization to remote service.
 *
 * @author John J. Franey
 */
@RequestScoped
public class TokenService {

    private final static Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Inject
    ServerToServerCodeFlow serverToServerCodeFlow;

    /**
     * Obtain a new token if it is unavailable from persistent store.
     * If from persistent store and is expired, then refresh it.
     *
     * @return
     */
    public Uni<String> getToken() {

        return serverToServerCodeFlow.getToken()
                .onItem().transformToUni(t -> Uni.createFrom().item(t.accessToken()));
    }



}
