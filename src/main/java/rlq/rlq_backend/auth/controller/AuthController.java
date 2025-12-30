package rlq.rlq_backend.auth.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import rlq.rlq_backend.auth.DTO.*;
import rlq.rlq_backend.exception.BusinessException;
import rlq.rlq_backend.security.JWTUtil;
import rlq.rlq_backend.security.authentication.CustomUserDetails;

@RestController
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @RequestBody AuthenticationRequestDTO authenticationRequestDTO) throws Exception {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequestDTO.getUsername(),
                            authenticationRequestDTO.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequestDTO.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new AuthenticationResponseDTO(token));
        } catch (AuthenticationException e) {
            throw new BusinessException("Incorrect username or password", e);
        }
    }
}
