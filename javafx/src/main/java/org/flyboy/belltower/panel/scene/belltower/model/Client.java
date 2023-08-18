package org.flyboy.belltower.panel.scene.belltower.model;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Per RestEasy/Quarkus extension, this interface
 * describes the methods available on the belltowerClient endpoint.
 *
 * @author John J. Franey
 */
@SuppressWarnings("QsUndeclaredPathMimeTypesInspection")
@Path("/belltower")
@RegisterRestClient
public interface Client {

    @GET
    Uni<Status> getStatus();

    @PUT
    @Path("/ring")
    Uni<Status> ring(@QueryParam("pattern") String pattern);
}
