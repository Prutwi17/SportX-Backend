package com.sportx.backend.util;

import com.sportx.backend.security.SecurityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public final class AuthUtil {

    private AuthUtil() {
    }

    public static String currentUserEmail() {
        return SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UsernameNotFoundException("User not authenticated"));
    }
}
