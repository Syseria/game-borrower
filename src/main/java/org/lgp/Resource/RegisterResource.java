package org.lgp.Resource;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lgp.Entity.User;
import org.lgp.Service.UserService;


@Path("/register")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegisterResource {

    @Inject
    UserService userService;

    @POST
    public Response register(User.RegisterRequestDTO request) throws FirebaseAuthException {
        String uid = userService.registerUser(request);
        return Response.ok(uid).build();
    }
}
