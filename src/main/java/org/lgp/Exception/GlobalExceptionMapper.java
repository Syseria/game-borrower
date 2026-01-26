package org.lgp.Exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    Logger logger;

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException webAppEx) {
            Response original =  webAppEx.getResponse();

            return Response.status(original.getStatus())
                    .entity(new ErrorResponse("not-found-error", exception.getMessage()))
                    .build();
        }

        // If we got here, it means no other specific mapper claimed this exception.
        logger.error("Unexpected System Error", exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("internal-server-error", "An unexpected error occurred."))
                .build();
    }
}