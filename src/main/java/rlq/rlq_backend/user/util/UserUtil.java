package rlq.rlq_backend.user.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import rlq.rlq_backend.exception.BusinessException;
import rlq.rlq_backend.security.authentication.CustomUserDetails;
import rlq.rlq_backend.user.entity.User;

@Service
@RequiredArgsConstructor
public class UserUtil {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }

        throw new IllegalStateException("Principal is not of type CustomUserDetails");
    }
}
