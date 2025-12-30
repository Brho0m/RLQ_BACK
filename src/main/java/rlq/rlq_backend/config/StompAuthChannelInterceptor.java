package rlq.rlq_backend.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import rlq.rlq_backend.security.JWTUtil;

@Component
@AllArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JWTUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = accessor.getFirstNativeHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String token = auth.substring(7);

            try {
                // Extract claims from token
                Claims claims = jwtUtil.extractAllClaims(token);
                String username = jwtUtil.extractUsername(token);
                
                // Extract userId from claims
                Long userId = null;
                Object userIdObj = claims.get("userId");
                if (userIdObj != null) {
                    if (userIdObj instanceof Integer) {
                        userId = ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        userId = (Long) userIdObj;
                    }
                }

                if (username == null || userId == null) {
                    throw new IllegalArgumentException("Invalid token: missing username or userId");
                }

                // Set the authenticated user principal
                accessor.setUser(new StompUserPrincipal(userId, username));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid or expired token: " + e.getMessage());
            }
        }

        return message;
    }
}
