package org.lgp.Resource;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.lgp.Entity.User;
import org.lgp.Service.UserService;

@Path("/user")
public class UserResource {

    @Inject
    UserService userService;

    @Inject
    SecurityIdentity identity;

    @POST
    @Path("/register")
    public Response register(User.RegisterRequestDTO request) throws FirebaseAuthException {
        String uid = userService.registerUser(request);
        return Response.ok(uid).build();
    }

    @GET
    @Path("/me")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public User getMyProfile() {
        String uid = identity.getPrincipal().getName();

        return userService.getUser(uid);
    }
}