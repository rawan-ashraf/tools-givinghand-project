package com.example.givinghand.util;

import jakarta.ws.rs.core.SecurityContext;

public class SecurityUtil {

    public static String getEmail(SecurityContext sc) {
        return sc.getUserPrincipal() != null
                ? sc.getUserPrincipal().getName()
                : null;
    }

    public static boolean isDonor(SecurityContext sc) {
        return sc.isUserInRole("donor");
    }

    public static boolean isOrganization(SecurityContext sc) {
        return sc.isUserInRole("organization");
    }
}