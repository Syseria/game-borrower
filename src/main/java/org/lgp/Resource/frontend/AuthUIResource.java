package org.lgp.Resource.frontend;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/")
public class AuthUIResource {

    @Inject
    SecurityIdentity identity;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance login();
        public static native TemplateInstance signup();
    }

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Object loginPage() {
        if (!identity.isAnonymous()) return Response.seeOther(URI.create("/catalog")).build();
        return Templates.login().data("identity", identity);
    }

    @GET
    @Path("/signup")
    @Produces(MediaType.TEXT_HTML)
    public Object signupPage() {
        if (!identity.isAnonymous()) return Response.seeOther(URI.create("/catalog")).build();
        return Templates.signup().data("identity", identity);
    }
}