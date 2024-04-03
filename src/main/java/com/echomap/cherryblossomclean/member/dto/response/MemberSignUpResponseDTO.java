package com.echomap.cherryblossomclean.member.dto.response;

import com.echomap.cherryblossomclean.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Builder
public class MemberSignUpResponseDTO {

  private String email;
  private String userName;

  @JsonProperty("join-date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime joinDate;

  public MemberSignUpResponseDTO(Member member) {
    this.email = member.getEmail();
    this.userName = member.getUserName();
    this.joinDate = member.getJoinDate();
  }
}
