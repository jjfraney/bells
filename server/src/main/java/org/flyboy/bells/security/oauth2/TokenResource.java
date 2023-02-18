package org.flyboy.bells.security.oauth2;

import io.smallrye.mutiny.Uni;
import org.flyboy.bells.tower.ErrorResponseBody;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public Uni<String> getToken() {
        return  tokenService.getToken();
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
