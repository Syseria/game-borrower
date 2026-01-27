package org.lgp.Security;

import com.google.firebase.auth.FirebaseAuth;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.Set;

@ApplicationScoped
public class FirebaseAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
                                              io.quarkus.security.identity.IdentityProviderManager identityProviderManager) {

        var cookie = context.request().getCookie("session");

        if (cookie == null) {
            return Uni.createFrom().optional(java.util.Optional.empty());
        }

        return Uni.createFrom().item(() -> {
                    try {
                        return FirebaseAuth.getInstance()
                                .verifySessionCookie(cookie.getValue(), true);
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid Token", e);
                    }
                })
                .runSubscriptionOn(io.smallrye.mutiny.infrastructure.Infrastructure.getDefaultWorkerPool()) // Run on worker thread
                .onItem().transform(token -> {
                    return (SecurityIdentity) QuarkusSecurityIdentity.builder()
                            .setPrincipal(token::getUid)
                            .addAttribute("email", token.getEmail())
                            .build();
                })
                .onFailure().recoverWithNull();
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Collections.emptySet();
    }
}