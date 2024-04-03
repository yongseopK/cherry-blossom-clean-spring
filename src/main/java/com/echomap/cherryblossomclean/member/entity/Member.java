package com.echomap.cherryblossomclean.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static com.echomap.cherryblossomclean.member.entity.Member.Role.COMMON;

@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "email")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_member")
@ToString
public class Member {

    @Id
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String userName;

    @CreationTimestamp
    private LocalDateTime joinDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = COMMON;

    public enum Role {
        ADMIN, COMMON;
    }
}
