package com.example.givinghand.security;

import java.util.Base64;

public class TokenUtil {


    public static String generateToken(String email, String role) {
        String data = email + ":" + role + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    public static String[] decode(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            return decoded.split(":");
        } catch (Exception e) {
            return null;
        }
    }

    public static String getEmail(String token) {
        String[] d = decode(token);
        return (d != null) ? d[0] : null;
    }

    public static String getRole(String token) {
        String[] d = decode(token);
        return (d != null) ? d[1] : null;
    }
}