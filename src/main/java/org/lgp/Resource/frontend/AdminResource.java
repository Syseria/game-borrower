package org.lgp.Resource.frontend;

import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/admin")
@RolesAllowed("maintainer")
public class AdminResource {

    @GET
    @Path("/inventory")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance manageInventory() {
        return io.quarkus.qute.Qute.fmt("admin/inventory").instance();
    }

    @GET
    @Path("/users")
    @RolesAllowed("admin")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance manageUsers() {
        return io.quarkus.qute.Qute.fmt("admin/users").instance();
    }
}