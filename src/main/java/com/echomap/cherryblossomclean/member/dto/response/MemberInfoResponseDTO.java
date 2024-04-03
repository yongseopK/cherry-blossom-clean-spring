package com.echomap.cherryblossomclean.member.dto.response;

import com.echomap.cherryblossomclean.member.entity.Member;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberInfoResponseDTO {

    private String email;
    private String userName;
    private LocalDateTime joinDate;

    public MemberInfoResponseDTO(Member member) {
        this.email = member.getEmail();
        this.userName = member.getUserName();
        this.joinDate = member.getJoinDate();
    }
}
