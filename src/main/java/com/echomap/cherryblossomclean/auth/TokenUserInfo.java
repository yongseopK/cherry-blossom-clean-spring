package com.echomap.cherryblossomclean.auth;

import com.echomap.cherryblossomclean.member.entity.Member;
import lombok.*;

import static com.echomap.cherryblossomclean.member.entity.Member.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class TokenUserInfo {

    private String email;
    private Role role;
}
