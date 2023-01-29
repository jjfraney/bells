package org.flyboy.bells.security.oauth2;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.http.HttpServer;
import io.vertx.mutiny.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class implements a code callback endpoint.
 * After user authorization, the auth server sends the code
 * to this endpoint..
 * @author John J. Franey
 */
@Dependent
public class CodeCallbackEndpoint {

    private final static Logger logger = LoggerFactory.getLogger(CodeCallbackEndpoint.class);
    private HttpServer httpServer;


    private static final Consumer<String> DEFAULT_CODE_CALLBACK = (code) -> logger.error("callback not available: {}", code);
    private Consumer<String> codeCallback = DEFAULT_CODE_CALLBACK;

    public void setCodeCallback(Consumer<String> codeCallback) {
        this.codeCallback = codeCallback;
    }

    /**
     * Enables the callback endpoint on a random available port on localhost.
     * Disable the endpoint automatically after request arrives.
     * @param path of the endpoint uri.
     */
    public Uni<HttpServer> enable(String path, String expectedState) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(expectedState);

        Vertx vertx = CDI.current().select(Vertx.class).get();

        httpServer = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.get("/" + path).handler(rc -> {
            Optional<String> queryCode = rc.queryParam("code").stream().findFirst();
            Optional<String> queryState = rc.queryParam("state").stream().findFirst();
            logger.debug("info: code: {}, state: {}", queryCode, queryState);

            // invoke code callback only when query 'state' matches the expected state
            // and query 'code' is present
            queryState.stream()
                    .filter(expectedState::equals)
                    .forEach(s -> queryCode.stream()
                            .filter(c -> c.length() > 0)
                            .forEach(codeCallback)
            );

            rc.response().end("bye bye")
                    .subscribe()
                    .with(
                            s -> logger.debug("returned response"),
                            f -> logger.debug("unable to return response", f)
                    );
        });

        // listen on any available port.
        return httpServer
                .requestHandler(router)
                .listen(0);

    }

    public void disable() {
        Objects.requireNonNull(httpServer);
        logger.info("disable httpServer");
        httpServer.close().subscribe().with(
                s -> logger.info("http server is closed"),
                f -> logger.error("http server close.", f)
        );
    }

    /**
     *
     * @param path of the auth redirect uri
     * @return URL of the callback endpoint.
     */
    public String getUri(String path) {
        Objects.requireNonNull(httpServer);
        int port = httpServer.actualPort();

        // "https://localhost:${port}/${path}"
        return "https://" +
                "localhost" +
                ":" +
                port +
                "/" +
                path;
    }

}
