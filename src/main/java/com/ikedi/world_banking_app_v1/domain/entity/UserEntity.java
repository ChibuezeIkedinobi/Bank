package com.ikedi.world_banking_app_v1.domain.entity;

import com.ikedi.world_banking_app_v1.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class UserEntity extends BaseClass {

    private String firstName;
    private String lastName;
    private String otherName;
    private String email;
    private String password;
    private String gender;
    private String address;
    private String stateOfOrigin;
    private BigDecimal accountBalance;
    private String phoneNumber;
    private String accountNumber;
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();


}
