package rlq.rlq_backend.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.Data;
import rlq.rlq_backend.user.enums.Roles;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column()
    private String firstName;

    @Column()
    private String lastName;

    @Column()
    @Enumerated(EnumType.STRING)
    private Roles role;

}
