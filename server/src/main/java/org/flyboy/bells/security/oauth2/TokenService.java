package org.flyboy.bells.security.oauth2;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.Duration;

/**
 * Obtains an access token for authentication/authorization to remote service.
 *
 * @author John J. Franey
 */
@RequestScoped
public class TokenService {

    private final static Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Inject
    CodeCallbackEndpoint callbackEndpoint;

    public Uni<String> getToken(String path) {
        String state = "20";

        Duration waitTime = Duration.ofSeconds(30);

        // createHttpServer, get server and pass to new Unis, browseAuthCode, codeEmiter -> exchangeCodeForToken
        //     -> codeEmiter -> exchangeCodeForToken
        //     -> browseAuthCode
        return callbackEndpoint.enable("stop", state)
                .onItem().call(this::browseAuthUrl)
                .onItem().transformToUni(this::emitCode)
                .ifNoItem().after(waitTime).failWith(new AuthCodeTimeoutException(waitTime))

                .onItem().transform(code -> {
                    logger.debug("this is the code: {}", code);
                    return code;
                })
        ;
    }

    /**
     * create an Uni which will emit the code.
     * The {@link CodeCallbackEndpoint} receives the code,
     * and will invoke the callback handler created here.
     * This code callback handler will use a Uni Emitter to
     * emit the code down the mutiny pipeline.
     * @param htppServer to observe
     * @return OAuth2 authorization code
     */
    private Uni<String> emitCode(HttpServer htppServer) {
        return Uni.createFrom().emitter(e -> {
            callbackEndpoint.setCodeCallback(e::complete);
            e.onTermination(callbackEndpoint::disable);
        });
    }

    private Uni<?> browseAuthUrl(HttpServer httpServer) {
        // for now, just get the uri and log it.
        // later: use desktop browser to browse the auth url
        int port = httpServer.actualPort();
        String authRedirectUri = "localhost:" + port;
        logger.info("callbackUri: {}", authRedirectUri);
        return Uni.createFrom().nullItem();
    }
}
