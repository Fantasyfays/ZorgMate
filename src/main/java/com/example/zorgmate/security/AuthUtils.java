package com.example.zorgmate.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth != null ? auth.getName() : null;
        System.out.println("👤 Huidige ingelogde gebruiker (via AuthUtils): " + name);
        return name;
    }
}
