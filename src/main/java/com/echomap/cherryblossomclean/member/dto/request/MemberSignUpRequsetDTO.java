package com.echomap.cherryblossomclean.member.dto.request;

import com.echomap.cherryblossomclean.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSignUpRequsetDTO {

  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Size(min = 8, max = 20)
  private String password;

  @NotBlank
  @Size(min = 2, max = 5)
  private String userName;

  public Member toEntity(PasswordEncoder passwordEncoder) {
    return Member.builder()
            .email(this.email)
            .password(passwordEncoder.encode(this.password))
            .userName(this.userName)
            .build();
  }
}
