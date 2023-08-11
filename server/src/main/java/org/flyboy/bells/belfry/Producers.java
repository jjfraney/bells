package org.flyboy.bells.belfry;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.net.NetClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;

/**
 * @author John J. Franey
 */
@ApplicationScoped
public class Producers {

    @Inject
    Vertx vertx;

    @Produces
    @ApplicationScoped
    public NetClient getNetClient() {
        return vertx.createNetClient();
    }
}
