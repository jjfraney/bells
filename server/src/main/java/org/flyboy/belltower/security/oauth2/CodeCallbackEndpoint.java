package org.flyboy.belltower.security.oauth2;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.http.HttpServer;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.CDI;
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
    @ConfigProperty(name = "belltower.security.oauth2.CodeCallbackEndpoint.path", defaultValue ="callback")
    String path;

    HttpServer httpServer;


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
    public record Info(String redirectUri, String state) {}

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
     * @param expectedState sent in authorization request and expected in callback
     * @return a routing context for httpserver
     */
    private Consumer<RoutingContext> handler(String expectedState) {
        return rc -> {
            List<String> states = rc.queryParam("state");
            String state = states.size() == 1 ? states.get(0) : null;

            List<String> codes = rc.queryParam("code");
            String code = codes.size() == 1 ? codes.get(0) : null;

            // send response first
            rc.response().end("bye bye")
                    .subscribe().with(
                            s -> logger.debug("sent response"),
                            f -> logger.error("fail sending response.", f)
                    );

            if(isValidCode(expectedState, state, code)) {
                codeCallback.accept(code);
            }
        };
    }

    boolean isValidCode(String expectedState, String state, String code) {
        boolean goodCode = code != null && code.length() > 0;
        if(! goodCode) {
            logger.warn("Code has no value or length: code: {}", code);
        }
        boolean goodState = expectedState.equals(state);
        if(! goodState) {
            logger.warn("State has no value or is not equal to expected state.  state: {}, expected state: {}", state, expectedState);
        }
        return goodCode && goodState;
    }


    /**
     * create a Uni which will emit the code downstream.
     * The {@link CodeCallbackEndpoint} receives the code from remote sender,
     * This code callback handler will complete a Uni Emitter.
     * @return OAuth2 authorization code
     */
    Uni<String> createCodeEmitter() {
        Uni<String> emitter = Uni.createFrom().emitter(e -> setCodeCallback(e::complete));

        return emitter
                .ifNoItem().after(waitTime).failWith(new AuthCodeTimeoutException(waitTime))
                .eventually(() -> {
                    logger.info("Shutting down code callback endpoint.");
                    return httpServer.close();
                })
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

    String generateState() {
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
