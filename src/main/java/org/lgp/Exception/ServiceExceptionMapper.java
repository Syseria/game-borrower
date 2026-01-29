package org.lgp.Exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {

    @Inject
    Logger logger;

    @Override
    public Response toResponse(ServiceException exception) {
        logger.error("Service Layer Error", exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("internal-error", "A technical error occurred while processing your request."))
                .build();
    }
}