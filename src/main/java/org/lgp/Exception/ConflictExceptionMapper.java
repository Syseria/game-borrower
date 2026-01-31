package org.lgp.Exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {

    @Inject
    Logger logger;

    @Override
    public Response toResponse(ConflictException exception) {
        logger.warnf("Conflict error [%s]: %s", exception.getErrorCode(), exception.getMessage());

        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(exception.getErrorCode(), exception.getMessage()))
                .build();
    }
}