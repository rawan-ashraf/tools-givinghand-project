package com.example.givinghand.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String auth = requestContext.getHeaderString("Authorization");

        if (auth == null || !auth.startsWith("Bearer ")) {
            return;
        }

        String token = auth.substring(7);

        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(token));
        } catch (Exception e) {
            abort(requestContext, "Invalid token");
            return;
        }

        String[] data = decoded.split(":");

        if (data.length < 2) {
            abort(requestContext, "Invalid token format");
            return;
        }

        String email = data[0];
        String role = data[1];

        if (email == null || role == null || email.isEmpty() || role.isEmpty()) {
            abort(requestContext, "Invalid token data");
            return;
        }

        Principal principal = () -> email;

        String finalRole = role;

        SecurityContext sc = new SecurityContext() {

            @Override
            public Principal getUserPrincipal() {
                return principal;
            }

            @Override
            public boolean isUserInRole(String r) {
                return finalRole != null && finalRole.equalsIgnoreCase(r);
            }

            @Override
            public boolean isSecure() {
                return true;
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        };

        requestContext.setSecurityContext(sc);
    }

    private void abort(ContainerRequestContext ctx, String msg) {
        ctx.abortWith(
                jakarta.ws.rs.core.Response.status(401)
                        .entity("{\"message\":\"" + msg + "\"}")
                        .build()
        );
    }
}