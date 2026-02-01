package org.lgp.Resource;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lgp.Entity.User;
import org.lgp.Entity.User.UpdateProfileRequestDTO;
import org.lgp.Entity.User.UpdateEmailRequestDTO;
import org.lgp.Entity.User.UpdatePasswordRequestDTO;
import org.lgp.Entity.User.UserProfileResponseDTO;
import org.lgp.Entity.User.UpdateRolesRequestDTO;
import org.lgp.Exception.ErrorResponse;
import org.lgp.Service.UserService;
import java.util.List;

@Path("/users")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @Inject
    SecurityIdentity identity;

    // =========================================================================
    // ME ENDPOINTS (Personal Profile)
    // =========================================================================

    @GET
    @Path("/profile")
    public UserProfileResponseDTO getMyProfile() {
        User.UserSearchCriteria criteria = User.UserSearchCriteria.builder()
                .id(identity.getPrincipal().getName())
                .build();
        return userService.searchUsers(criteria).getFirst();
    }

    @PATCH
    @Path("/profile")
    public Response updateMyProfile(UpdateProfileRequestDTO request) {
        String uid = identity.getPrincipal().getName();
        userService.updateProfile(uid, request);
        return Response.ok().build();
    }

    @PUT
    @Path("/profile/email")
    public Response updateMyEmail(@Valid UpdateEmailRequestDTO request) {
        String uid = identity.getPrincipal().getName();
        userService.updateEmail(uid, request);
        return Response.ok().build();
    }

    @PUT
    @Path("/profile/password")
    public Response updateMyPassword(@Valid UpdatePasswordRequestDTO request) {
        String uid = identity.getPrincipal().getName();
        userService.updatePassword(uid, request);
        return Response.ok().build();
    }

    // =========================================================================
    // ADMIN/MANAGEMENT ENDPOINTS
    // =========================================================================

    @GET
    @RolesAllowed("admin")
    public List<UserProfileResponseDTO> search(
            @QueryParam("uid") String uid,
            @QueryParam("name") String name,
            @QueryParam("lname") String lname,
            @QueryParam("email") String email,
            @QueryParam("role") String roleStr
    ) {
        User.Role role = (roleStr != null) ? User.Role.fromString(roleStr) : null;
        User.UserSearchCriteria criteria = User.UserSearchCriteria.builder()
                .id(uid)
                .name(name)
                .lname(lname)
                .email(email)
                .role(role)
                .build();

        return userService.searchUsers(criteria);
    }

    @PATCH
    @Path("/{uid}/profile")
    @RolesAllowed("admin")
    public Response updateProfile(@PathParam("uid") String uid, UpdateProfileRequestDTO request) {
        userService.updateProfile(uid, request);
        return Response.ok().build();
    }

    @PUT
    @Path("/{uid}/email")
    @RolesAllowed("admin")
    public Response updateEmail(@PathParam("uid") String uid, @Valid UpdateEmailRequestDTO request) {
        userService.updateEmail(uid, request);
        return Response.ok().build();
    }

    @PUT
    @Path("/{uid}/password")
    @RolesAllowed("admin")
    public Response updatePassword(@PathParam("uid") String uid, @Valid UpdatePasswordRequestDTO request) {
        userService.updatePassword(uid, request);
        return Response.ok().build();
    }

    @PUT
    @Path("/{uid}/roles")
    @RolesAllowed("admin")
    public Response updateRoles(@PathParam("uid") String uid, @Valid UpdateRolesRequestDTO request) {
        try {
            userService.updateRoles(uid, request);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("invalid-role", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{uid}")
    @RolesAllowed("admin")
    public Response delete(@PathParam("uid") String uid) {
        userService.deleteUser(uid);
        return Response.noContent().build();
    }
}