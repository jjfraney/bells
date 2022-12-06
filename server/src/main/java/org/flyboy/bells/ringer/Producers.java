package org.flyboy.bells.ringer;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.net.NetClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Produces;

/**
 * @author John J. Franey
 */
@ApplicationScoped
public class Producers {

    @Inject
    Vertx vertx;

    @Produces @ApplicationScoped
    public NetClient getNetClient() {
        return vertx.createNetClient();
    }
}
