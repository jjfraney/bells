package org.flyboy.belltower.panel.tower;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Per RestEasy/Quarkus extension, this interface
 * describes the methods available on the belltower endpoint.
 *
 * @author John J. Franey
 */
@SuppressWarnings("QsUndeclaredPathMimeTypesInspection")
@Path("/belltower")
@RegisterRestClient
public interface Belltower {

    @GET
    Uni<BelltowerStatus> getStatus();

    @PUT
    @Path("/ring")
    Uni<BelltowerStatus> ring(@QueryParam("pattern") String pattern);
}
