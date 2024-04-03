package com.echomap.cherryblossomclean.member.dto.response;

import com.echomap.cherryblossomclean.member.entity.Member;
import lombok.*;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberModifyResponseDTO {

    private String email;
    private String userName;
    private String role;
    private String token;

    public MemberModifyResponseDTO(Member member, String token) {
        this.email = member.getEmail();
        this.userName = member.getUserName();
        this.role = member.getRole().toString();
        this.token = token;
    }
}
