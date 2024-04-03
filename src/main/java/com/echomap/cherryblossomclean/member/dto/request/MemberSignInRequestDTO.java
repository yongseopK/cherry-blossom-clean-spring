package com.echomap.cherryblossomclean.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSignInRequestDTO {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
