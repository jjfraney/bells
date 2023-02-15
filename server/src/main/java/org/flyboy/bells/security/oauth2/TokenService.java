package org.flyboy.bells.security.oauth2;

import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.ZonedDateTime;

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

    @Inject
    TokenStore tokenStore;

    /**
     * Obtain a new token if it is unavailable from persistent store.
     * If from persistent store and is expired, then refresh it.
     *
     * @return access token
     */
    public Uni<String> getToken() {
        return Uni.createFrom().item(tokenStore.isPresent())
                .onItem().transformToUni(isStored -> {
                    return isStored ? tokensFromStore() : newTokens();
                })
                .onItem().transform(Tokens::accessToken);
    }

    /**
     * get new tokens from implementation of server to server code flow.
     * @return
     */
    private Uni<Tokens> newTokens() {
        return serverToServerCodeFlow.getToken()
                .onItem().invoke(t -> tokenStore.store(t));
    }

    /**
     * read the token from storage, if still valid, return it.  Otherwise, refresh,
     * store and return the new tokens.
     * @return
     */
    private Uni<Tokens> tokensFromStore() {
        return Uni.createFrom().item(tokenStore.read())
                .onItem().transformToUni(tokens -> {
                    // if expired
                    if(ZonedDateTime.now().isAfter(tokens.expires())) {
                        // return refreshed tokens
                        return serverToServerCodeFlow.refresh(tokens)
                                .onItem().invoke(tokenStore::store);
                    } else {
                        // return existing tokens
                        return Uni.createFrom().item(tokens);
                    }
                });
    }




}