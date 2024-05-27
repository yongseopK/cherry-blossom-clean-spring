package com.echomap.cherryblossomclean.member.service;

import com.echomap.cherryblossomclean.auth.TokenProvider;
import com.echomap.cherryblossomclean.auth.TokenUserInfo;
import com.echomap.cherryblossomclean.exception.ValidateEmailException;
import com.echomap.cherryblossomclean.member.dto.request.MemberModifyRequestDTO;
import com.echomap.cherryblossomclean.member.dto.request.MemberSignInRequestDTO;
import com.echomap.cherryblossomclean.member.dto.request.MemberSignUpRequsetDTO;
import com.echomap.cherryblossomclean.member.dto.response.*;
import com.echomap.cherryblossomclean.member.entity.Member;
import com.echomap.cherryblossomclean.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;
  private final JavaMailSender mailSender;

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=";
  private static final int PASSWORD_LENGTH = 8;
  private static final SecureRandom random = new SecureRandom();

  static final String EMAIL_REGEX =
      "^[a-zA-Z0-9_+&*-]+(?:\\."
          + "[a-zA-Z0-9_+&*-]+)*@"
          + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
          + "A-Z]{2,7}$";

  public MemberSignUpResponseDTO create(MemberSignUpRequsetDTO dto) {
    if (dto == null) {
      throw new RuntimeException("입력된 정보가 없습니다.");
    }

    String email = dto.getEmail();

    if (memberRepository.existsByEmail(email)) {
      log.warn("이메일이 중복되었습니다. : {}", email);
      throw new RuntimeException("중복된 이메일입니다.");
    }

    Member save = memberRepository.save(dto.toEntity(passwordEncoder));

    log.info("회원가입 성공 user : {}", save.getUserName());

    return new MemberSignUpResponseDTO(save);
  }

  public boolean isDuplicateEmail(String email) {
    if (!email.matches(EMAIL_REGEX)) {
      throw new ValidateEmailException("올바른 형식의 이메일 주소가 아닙니다.");
    } else {
      return memberRepository.existsByEmail(email);
    }
  }

  @Transactional
  public MemberModifyResponseDTO modify(MemberModifyRequestDTO dto, TokenUserInfo userInfo) {
    if (dto == null) {
      throw new IllegalArgumentException("수정된 회원정보가 없습니다.");
    }

    Member member =
        memberRepository
            .findByEmail(userInfo.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("해당 이메일에 대한 계정이 없습니다."));

    log.info("수정하는 회원 이름: {}", member.getUserName());

    if (dto.getPassword() != null) {
      member.setPassword(passwordEncoder.encode(dto.getPassword()));
    }

    if (dto.getUserName() != null) {
      member.setUserName(dto.getUserName());
    }

    log.info("수정된 회원 정보: {}", member);

    Member savedMember = memberRepository.save(member);
    String token = tokenProvider.createToken(savedMember, false);

    return new MemberModifyResponseDTO(savedMember, token);
  }

  @Transactional(readOnly = true)
  public MemberListResponseDTO getAllMembers(TokenUserInfo userInfo) {

    Optional<Member> foundAdmin = memberRepository.findByEmail(userInfo.getEmail());

    foundAdmin.ifPresent(
        admin -> {
          if (admin.getRole() != Member.Role.ADMIN) {
            throw new RuntimeException("운영자만 시도할 수 있는 요청입니다.");
          }
        });

    List<Member> members = memberRepository.findAll();

    List<MemberInfoResponseDTO> list = members.stream().map(MemberInfoResponseDTO::new).toList();

    return MemberListResponseDTO.builder().members(list).build();
  }

  public void updateMember(String email, TokenUserInfo userInfo) {
    Optional<Member> foundMember = memberRepository.findByEmail(userInfo.getEmail());

    foundMember.ifPresent(
        member -> {
          if (member.getRole() != Member.Role.ADMIN) {
            throw new RuntimeException("운영자만 시도할 수 있는 요청입니다.");
          }

          Optional<Member> searchMember = memberRepository.findByEmail(email);

          searchMember.ifPresent(
              member1 -> {
                if (!member1.isStatus()) {
                  member1.setStatus(true);
                  member1.setUpdatedAt(LocalDateTime.now());
                  memberRepository.save(member1);
                } else {
                  member1.setStatus(false);
                  member1.setUpdatedAt(null);
                  memberRepository.save(member1);
                }
              });

          if (searchMember.isEmpty()) {
            throw new RuntimeException("해당 회원을 찾을 수 없습니다.");
          }
        });

    if (foundMember.isEmpty()) {
      throw new RuntimeException("운영자를 찾을 수 없습니다.");
    }
  }

  public void changeToRole(TokenUserInfo userInfo, String email) {
    Optional<Member> foundAdmin = memberRepository.findByEmail(userInfo.getEmail());

    foundAdmin.ifPresent(
        admin -> {
          if (admin.getRole() != Member.Role.ADMIN) {
            throw new RuntimeException("운영자만 시도할 수 있는 요청입니다.");
          }

          Optional<Member> foundMember = memberRepository.findByEmail(email);

          foundMember.ifPresent(
              member -> {
                if (member.getRole() == Member.Role.ADMIN) {
                  member.setRole(Member.Role.COMMON);
                } else if (member.getRole() == Member.Role.COMMON) {
                  member.setRole(Member.Role.ADMIN);
                }

                Member save = memberRepository.save(member);
                tokenProvider.createToken(save, false);
              });

          if (foundMember.isEmpty()) {
            throw new RuntimeException("해당 유저를 찾을 수 없습니다.");
          }
        });

    if (foundAdmin.isEmpty()) {
      throw new RuntimeException("운영자를 찾을 수 없습니다.");
    }
  }

  @Transactional
  public void requestWithdrawal(TokenUserInfo userInfo) {
    Member member =
        memberRepository
            .findByEmail(userInfo.getEmail())
            .orElseThrow(() -> new RuntimeException("회원정보를 찾을 수 없습니다."));

    member.setWithdrawalRequested(true);
    member.setWithdrawalDate(LocalDateTime.now());
    memberRepository.save(member);
  }

  @Transactional
  public void cancelWithdrawal(MemberSignInRequestDTO dto) {
    Member member =
        memberRepository
            .findByEmail(dto.getEmail())
            .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다"));

    if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
      throw new RuntimeException("비밀번호가 일치하지 않습니다.");
    }

    member.setWithdrawalRequested(false);
    member.setWithdrawalDate(null);
    memberRepository.save(member);
  }

  public MemberModifyResponseDTO sendTemporaryPassword(String email) {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

    String temporaryPassword = generateTemporaryPassword();

    Member member = memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));

    member.setPassword(passwordEncoder.encode(temporaryPassword));

    Member savedMember = memberRepository.save(member);

    String htmlContent = "<html><body style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa; padding: 20px; margin: 0;\">" +
            "<div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 40px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);\">" +
            "<h1 style=\"color: #6c63ff; text-align: center; margin-bottom: 30px; font-size: 2.0em;\">깨끗한 꽃놀이 임시 비밀번호</h1>" +
            "<p style=\"font-size: 18px; line-height: 1.6; color: #333;\">안녕하세요,</p>" +
            "<p style=\"font-size: 18px; line-height: 1.6; color: #333;\">회원님의 임시 비밀번호를 알려드립니다.</p>" +
            "<div style=\"background-color: #f3f0ff; padding: 20px; border-radius: 5px; margin: 30px 0;\">" +
            "<p style=\"font-size: 24px; font-weight: bold; color: #6c63ff; text-align: center;\">" + temporaryPassword + "</p>" +
            "</div>" +
            "<p style=\"font-size: 18px; line-height: 1.6; color: #333;\">임시 비밀번호를 사용하여 로그인한 후, 반드시 비밀번호를 변경해주시기 바랍니다.</p>" +
            "<p style=\"font-size: 18px; line-height: 1.6; color: #333; margin: 20px 0px;\">감사합니다.</p>" +
            "<p style=\"font-size: 18px; line-height: 1.6; color: #333;\">깨끗한 꽃놀이 드림</p>" +
            "</div></body></html>";

    try {
      helper.setTo(email);
      helper.setSubject("깨끗한 꽃놀이 임시 비밀번호 안내");
      helper.setText(htmlContent, true);
      mailSender.send(message);
    } catch (MessagingException e) {
      throw new RuntimeException("임시 비밀번호 전송에 실패했습니다.", e);
    }

    String token = tokenProvider.createToken(savedMember, false);

    return new MemberModifyResponseDTO(savedMember, token);
  }

  public static String generateTemporaryPassword() {
    StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
    for (int i = 0; i < PASSWORD_LENGTH; i++) {
      int randomIndex = random.nextInt(CHARACTERS.length());
      sb.append(CHARACTERS.charAt(randomIndex));
    }
    return sb.toString();
  }
}
