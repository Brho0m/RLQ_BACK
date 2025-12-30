package rlq.rlq_backend.security.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rlq.rlq_backend.user.entity.User;
import rlq.rlq_backend.user.enums.Roles;
import rlq.rlq_backend.security.JWTUtil;

import java.io.IOException;

@AllArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {

    private JWTUtil jwtUtil;

    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Extract the Authorization header from the request
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Check if the Authorization header is present and starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extract the JWT
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                send401(response, "TOKEN_EXPIRED", "JWT token is expired");
                return;
            } catch (JwtException | IllegalArgumentException e) {
                // JwtException covers malformed/signature/unsupported/etc
                send401(response, "INVALID_TOKEN", "JWT token is invalid");
                return;
            }
        }

        // Validate the JWT and set the authentication context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(jwt, userDetails)) {
                Claims claims = jwtUtil.extractAllClaims(jwt);

                // Create a User entity with the extracted claims
                User user = new User();
                user.setUsername(username);
                user.setFirstName((String) claims.get("firstName"));
                user.setLastName((String) claims.get("lastName"));
                user.setRole(Roles.valueOf((String) claims.get("role")));
                user.setId(((Integer) claims.get("userId")).longValue());

                // Create CustomUserDetails with the User entity
                CustomUserDetails customUserDetails = new CustomUserDetails(user);

                // Create an authentication token
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        customUserDetails, null, customUserDetails.getAuthorities());

                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        // Continue the request chain
        chain.doFilter(request, response);
    }

    private void send401(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
            {"error":"%s","message":"%s"}
            """.formatted(code, message));
    }
}
