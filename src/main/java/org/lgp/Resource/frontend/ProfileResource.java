package org.lgp.Resource.frontend;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.lgp.DTO.UserSearchCriteria;
import org.lgp.Service.UserService;

@Path("/profile")
@Authenticated
public class ProfileResource {

    @Inject UserService userService;
    @Inject SecurityIdentity identity;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showProfile() {
        String uid = identity.getPrincipal().getName();
        var user = userService.searchUsers(UserSearchCriteria.builder().id(uid).build()).data().getFirst();
        return io.quarkus.qute.Qute.fmt("profile").data("user", user).instance();
    }
}