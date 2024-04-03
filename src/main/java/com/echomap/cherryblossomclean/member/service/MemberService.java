package com.echomap.cherryblossomclean.member.service;

import com.echomap.cherryblossomclean.auth.TokenProvider;
import com.echomap.cherryblossomclean.auth.TokenUserInfo;
import com.echomap.cherryblossomclean.exception.ValidateEmailException;
import com.echomap.cherryblossomclean.member.dto.request.MemberModifyRequestDTO;
import com.echomap.cherryblossomclean.member.dto.request.MemberSignInRequestDTO;
import com.echomap.cherryblossomclean.member.dto.request.MemberSignUpRequsetDTO;
import com.echomap.cherryblossomclean.member.dto.response.MemberInfoResponseDTO;
import com.echomap.cherryblossomclean.member.dto.response.MemberModifyResponseDTO;
import com.echomap.cherryblossomclean.member.dto.response.MemberSignInResponseDTO;
import com.echomap.cherryblossomclean.member.dto.response.MemberSignUpResponseDTO;
import com.echomap.cherryblossomclean.member.entity.Member;
import com.echomap.cherryblossomclean.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    final static String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\." +
                    "[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                    "A-Z]{2,7}$";


    public MemberSignUpResponseDTO create(MemberSignUpRequsetDTO dto) {
        if(dto == null) {
            throw new RuntimeException("입력된 정보가 없습니다.");
        }

        String email = dto.getEmail();

        if(memberRepository.existsByEmail(email)) {
            log.warn("이메일이 중복되었습니다. : {}", email);
            throw new RuntimeException("중복된 이메일입니다.");
        }


        Member save = memberRepository.save(dto.toEntity(passwordEncoder));

        log.info("회원가입 성공 user : {}", save.getUserName());

        return new MemberSignUpResponseDTO(save);
    }

    public boolean isDuplicateEmail(String email) {
        if(!email.matches(EMAIL_REGEX)) {
            throw new ValidateEmailException("올바른 형식의 이메일 주소가 아닙니다.");
        } else {
            return memberRepository.existsByEmail(email);
        }
    }

    public MemberSignInResponseDTO authenticate(MemberSignInRequestDTO dto) {
        if(!dto.getEmail().matches(EMAIL_REGEX)) {
            throw new ValidateEmailException("올바른 형식의 이메일 주소가 아닙니다.");
        }

        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new RuntimeException("가입된 회원이 아닙니다."));

        String inputPassword = dto.getPassword();
        String encodedPassword = member.getPassword();

        if(!passwordEncoder.matches(inputPassword, encodedPassword)) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        String token = tokenProvider.createToken(member);

        return new MemberSignInResponseDTO(member, token);
    }

    public MemberInfoResponseDTO findUserInfo(TokenUserInfo userInfo) {
        Member member = memberRepository.findByEmail(userInfo.getEmail()).orElseThrow(() -> new RuntimeException("정보와 일치한 회원이 없습니다."));

        return new MemberInfoResponseDTO(member);
    }

    @Transactional
    public MemberModifyResponseDTO modify(MemberModifyRequestDTO dto, TokenUserInfo userInfo) {

        if(dto == null) {
            throw new RuntimeException("수정된 회원정보가 없습니다.");
        }

        Member member = memberRepository.findByEmail(userInfo.getEmail()).orElseThrow(() -> new RuntimeException("해당 이메일에 대한 계정이 없습니다."));
        MemberModifyRequestDTO memberModifyRequestDTO = new MemberModifyRequestDTO(member);

        log.info("수정하는 회원 이름 : {}", member.getUserName());
        boolean flag = false;

        if(dto.getPassword() != null) {
            memberModifyRequestDTO.setPassword(dto.getPassword());
            flag = true;
        }
        if(dto.getUserName() != null) {
            memberModifyRequestDTO.setUserName(dto.getUserName());
        }

        log.info("수정된 회원 dto : {}", dto);

        Member saved = null;
        if(flag) {
            saved = memberRepository.save(memberModifyRequestDTO.toEntity(passwordEncoder));
        } else {
            saved = memberRepository.save(memberModifyRequestDTO.toEntity());
        }

        String token = tokenProvider.createToken(saved);

        return new MemberModifyResponseDTO(saved, token);
    }

    @Transactional
    public void delete(TokenUserInfo userInfo, String password) {
        Member member = memberRepository.findByEmail(userInfo.getEmail()).orElseThrow(() -> new RuntimeException("일치하는 회원정보가 없습니다."));

        String encodedPassword = member.getPassword();

        if(!passwordEncoder.matches(password, encodedPassword)) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        memberRepository.delete(member);
    }

}
