package org.flyboy.bells.security.oauth2;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.http.HttpServer;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

/**
 * This class implements a code callback endpoint.
 * @author John J. Franey
 */
@Dependent
public class CodeCallbackEndpoint {
    private final static Logger logger = LoggerFactory.getLogger(CodeCallbackEndpoint.class);

    /**
     * the wait time for the code call back
     */
    @ConfigProperty(name = "belltower.security.oauth2.CodeCallbackEndpoint.waitTime", defaultValue = "PT15S")
    Duration waitTime;

    /**
     * length of state query parameter
     */
    @ConfigProperty(name = "belltower.security.oauth2.CodeCallbackEndpoint.stateLength", defaultValue ="30")

    int stateLength;

    /**
     * path segment of callback uri.
     */
    @ConfigProperty(name = "belltower.ecurity.oauth2.CodeCallbackEndpoint.path", defaultValue ="callback")
    String path;

    private HttpServer httpServer;


    private static final Consumer<String> DEFAULT_CODE_CALLBACK = (code) -> logger.error("callback not available: {}", code);
    private Consumer<String> codeCallback = DEFAULT_CODE_CALLBACK;

    public void setCodeCallback(Consumer<String> codeCallback) {
        this.codeCallback = codeCallback;
    }

    /**
     * contains details of the callback endpoint for use in authorization request
     * @param redirectUri the redirect uri of the callback endpoint
     * @param state the expected state value which should be in the callback
     */
    public static record Info(String redirectUri, String state) {};

    /**
     * Enables the callback endpoint on a random available port on localhost.
     * Disable the endpoint automatically after request arrives.
     * It generates some values demanded by the authorization service:
     * redirect url, state,
     * and returns them after setting up the endpoint.
     */
    public Uni<Info> enable() {
        String expectedState = generateState();

        Vertx vertx = CDI.current().select(Vertx.class).get();
        httpServer = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.get("/" + path).handler(handler(expectedState));


        // listen on any available port,
        // return the uri.
        return httpServer
                .requestHandler(router)
                .listen(0)
                .onItem().transform(h -> new Info(getUri(), expectedState));

    }

    /**
     * handle the callback after authorization to get the code and emit it downstream.
     * @param expectedState
     * @return
     */
    private Consumer<RoutingContext> handler(String expectedState) {
        return rc -> {
            List<String> states = rc.queryParam("state");
            List<String> codes = rc.queryParam("code");

            // send response first
            rc.response().end("bye bye")
                    .subscribe().with(
                            s -> logger.debug("sent response"),
                            f -> logger.error("fail sending response.", f)
                    );

            // check state value to what we expecct
            if (codes.size() == 1 && states.size() == 1) {
                String code = codes.get(0);
                String state = states.get(0);
                if (expectedState.equals(state)) {
                    if (code.length() > 0) {
                        // emit the code
                        codeCallback.accept(code);
                    } else {
                        logger.warn("code parameter has no value.");
                    }
                } else {
                    logger.warn("state value does not match");
                }
            } else {
                logger.warn("missing or too many params for code or state. states count={}, codes count={}",
                        states.size(), codes.size());
            }

        };
    }


    /**
     * create a Uni which will emit the code downstream.
     * The {@link CodeCallbackEndpoint} receives the code from remote sender,
     * This code callback handler will complete a Uni Emitter.
     * @return OAuth2 authorization code
     */
    Uni<String> createCodeEmitter() {
        Uni<String> emitter = Uni.createFrom().emitter(e -> {
            setCodeCallback(e::complete);
        });

        return emitter
                .ifNoItem().after(waitTime).failWith(new AuthCodeTimeoutException(waitTime))
                .eventually(() -> httpServer.close())
                ;


    }

    /**
     *
     * @return URL of the callback endpoint.
     */
    public String getUri() {
        Objects.requireNonNull(httpServer);
        int port = httpServer.actualPort();

        // "https://localhost:${port}/${path}"
        return "http://" +
                "localhost" +
                ":" +
                port +
                "/" +
                path;
    }

    private String generateState() {
        int asciiZero = "0".getBytes(StandardCharsets.UTF_8)[0];
        int asciiLowerZee = "z".getBytes(StandardCharsets.UTF_8)[0];
        Random random = new Random();

        return random.ints(asciiZero, asciiLowerZee + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(stateLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


}
