package org.lgp.Exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Inject
    Logger logger;

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        logger.infof("Resource not found: %s", exception.getMessage());

        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("not-found", exception.getMessage()))
                .build();
    }
}