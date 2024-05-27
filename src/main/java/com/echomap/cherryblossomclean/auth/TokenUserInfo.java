package com.echomap.cherryblossomclean.auth;

import lombok.*;

import static com.echomap.cherryblossomclean.member.entity.Member.*;

@Setter
@Getter
@ToString(of = {"email", "role"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class TokenUserInfo {

    private String email;
    private Role role;
}
