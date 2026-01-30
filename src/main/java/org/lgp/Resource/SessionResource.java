package org.lgp.Resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.SessionCookieOptions;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import java.util.concurrent.TimeUnit;

@Path("/auth")
public class SessionResource {

    @Inject
    FirebaseAuth firebaseAuth;

    @Inject
    Logger logger;

    // =========================================================================
    // ENDPOINTS
    // =========================================================================

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginRequestDTO request) throws FirebaseAuthException {
        long expiresIn = TimeUnit.DAYS.toMillis(5);

        SessionCookieOptions options = SessionCookieOptions.builder()
                .setExpiresIn(expiresIn)
                .build();

        String sessionCookieValue = firebaseAuth.createSessionCookie(request.idToken, options);

        FirebaseToken decodedToken = firebaseAuth.verifySessionCookie(sessionCookieValue, false);
        String uid = decodedToken.getUid();

        logger.infof("Creating token for user: %s", uid);

        NewCookie cookie = new NewCookie.Builder("session")
                .value(sessionCookieValue)
                .path("/")
                .maxAge((int) TimeUnit.MILLISECONDS.toSeconds(expiresIn))
                .httpOnly(true)
                .secure(false)      // set to true in prod
                .build();

        return Response.ok("Logged in")
                .cookie(cookie)
                .build();
    }

    @POST
    @Path("/logout")
    public Response logout(@CookieParam("session") Cookie cookie) throws FirebaseAuthException {
        NewCookie logoutCookie = new NewCookie.Builder("session")
                .value("")
                .path("/")
                .maxAge(0)
                .build();

        if (cookie == null || cookie.getValue().isEmpty() || cookie.getValue() == null) {
            return Response.ok("Logged out").cookie(logoutCookie).build();
        }

        FirebaseToken decodedToken = firebaseAuth.verifySessionCookie(cookie.getValue(), false);
        String uid = decodedToken.getUid();

        firebaseAuth.revokeRefreshTokens(uid);

        logger.infof("Revoked tokens for user: %s", uid);

        return Response.ok("Logged out").cookie(logoutCookie).build();
    }

    // =========================================================================
    // DTOs
    // =========================================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LoginRequestDTO(String idToken) {}
}