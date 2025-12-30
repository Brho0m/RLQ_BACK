package rlq.rlq_backend.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import rlq.rlq_backend.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
