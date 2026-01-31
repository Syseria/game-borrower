package org.lgp.Exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Inject
    Logger logger;

    @Override
    public Response toResponse(ValidationException exception) {
        logger.infof("Validation error [%s]: %s", exception.getErrorCode(), exception.getMessage());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(exception.getErrorCode(), exception.getMessage()))
                .build();
    }
}