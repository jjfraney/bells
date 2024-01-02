package org.flyboy.belltower.security.oauth2;

import io.smallrye.mutiny.Uni;
import org.flyboy.belltower.belfry.ErrorResponseBody;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author John J. Franey
 */
@Path("/bell/token")
@Produces(MediaType.APPLICATION_JSON)
public class TokenResource {

    private static final Logger logger = LoggerFactory.getLogger(TokenResource.class);
    @Inject
    TokenService tokenService;

    @GET
    public Uni<Void> getToken() {
        return  tokenService.getToken()
                .onItem().transformToUni(token -> Uni.createFrom().nullItem());
    }

    @SuppressWarnings("unused")
    @ServerExceptionMapper
    public Response mapException(AuthCodeTimeoutException e) {
        ErrorResponseBody body = new ErrorResponseBody();
        body.setDetails(e.getMessage());
        logger.info("{}, error id: {}", body.getDetails(), body.getErrorId());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }

    @ServerExceptionMapper
    public Response mapException(AuthorizationException e) {
        ErrorResponseBody body = new ErrorResponseBody();
        body.setDetails(e.getMessage());
        logger.info("{}, error id: {}", body.getDetails(), body.getErrorId());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }
}
