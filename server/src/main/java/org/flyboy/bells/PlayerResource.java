package org.flyboy.bells;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.net.NetClient;
import io.vertx.mutiny.core.net.NetSocket;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.stream.Collectors;

@Path("/bells")                        // <1>
public class PlayerResource {

    private final NetClient netClient;

    PlayerResource(Vertx vertx) { // <2>
        this.netClient = vertx.createNetClient();
    }


    private static final int PORT = 6600;

    @GET
    @Path("/mpd/{cmd}")
    public Uni<String> mpd(@PathParam("cmd") String cmd) {
            return netClient.connect(PORT, "localhost")
                    .onItem().invoke(netSocket -> {
                        netSocket.writeAndForget(cmd + "\n");
                        netSocket.writeAndForget("close\n");
                    })
                    .onItem().transformToMulti(NetSocket::toMulti)
                    .onItem().transform(buffer -> buffer.toString())
                    .flatMap(s -> Multi.createFrom().items(s.split("\n")))
                    .skip().first()
                    .onItem().transform(s -> s + "\n")
                    .collect().with(Collectors.joining())
                    ;

    }
}
