package org.lgp.Resource;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.lgp.Entity.User;
import org.lgp.Service.UserService;

@Path("/user")
public class UserResource {

    @Inject
    UserService userService;

    @Inject
    SecurityIdentity identity;

    @GET
    @Path("/me")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public User getMyProfile() {
        String uid = identity.getPrincipal().getName();

        return userService.getUser(uid);
    }
}