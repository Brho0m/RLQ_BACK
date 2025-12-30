package rlq.rlq_backend.config;

import java.security.Principal;

public class StompUserPrincipal implements Principal {
    
    private final Long userId;
    private final String username;

    public StompUserPrincipal(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    @Override
    public String getName() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }
}

