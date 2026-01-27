package org.lgp.Exception;

import com.google.firebase.ErrorCode;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class FirebaseAuthExceptionMapper implements ExceptionMapper<FirebaseAuthException> {

    @Inject
    Logger logger;

    @Override
    public Response toResponse(FirebaseAuthException exception) {
        ErrorCode code = exception.getErrorCode();
        logger.warnf("Firebase Auth Error: %s", code);

        Response.Status status = switch (code) {
            case ALREADY_EXISTS -> Response.Status.CONFLICT;
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case INVALID_ARGUMENT -> Response.Status.BAD_REQUEST;
            case PERMISSION_DENIED -> Response.Status.FORBIDDEN;
            default -> Response.Status.BAD_REQUEST;
        };

        return Response.status(status)
                .entity(new ErrorResponse(code.name(), exception.getMessage()))
                .build();
    }
}