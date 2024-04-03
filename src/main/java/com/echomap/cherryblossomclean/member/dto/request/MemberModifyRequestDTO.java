package com.echomap.cherryblossomclean.member.dto.request;

import com.echomap.cherryblossomclean.member.entity.Member;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberModifyRequestDTO {

    private String email;

    private String password;

    private String userName;

    private LocalDateTime joinDate;

    public MemberModifyRequestDTO(Member member) {
        this.email = member.getEmail();
        this.password = member.getPassword();
        this.userName = member.getUserName();
        this.joinDate = member.getJoinDate();
    }

    // 비밀번호를 수정할때 재인코딩해서 엔터티화
    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .email(this.email)
                .password(passwordEncoder.encode(this.password))
                .userName(this.userName)
                .joinDate(this.joinDate)
                .build();
    }

    // 비밀번호를 수정하지 않을때 원래의 비밀번호로 덮어쓰기위함
    public Member toEntity() {
        return Member.builder()
                .email(this.email)
                .password(this.password)
                .userName(this.userName)
                .joinDate(this.joinDate)
                .build();
    }
}
