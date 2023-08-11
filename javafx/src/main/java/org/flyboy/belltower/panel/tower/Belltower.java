package org.flyboy.belltower.panel.tower;

import io.smallrye.mutiny.Uni;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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
