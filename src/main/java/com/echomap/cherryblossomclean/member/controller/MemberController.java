package com.echomap.cherryblossomclean.member.controller;

import com.echomap.cherryblossomclean.auth.TokenUserInfo;
import com.echomap.cherryblossomclean.exception.ValidateEmailException;
import com.echomap.cherryblossomclean.member.dto.request.MemberModifyRequestDTO;
import com.echomap.cherryblossomclean.member.dto.request.MemberSignInRequestDTO;
import com.echomap.cherryblossomclean.member.dto.request.MemberSignUpRequsetDTO;
import com.echomap.cherryblossomclean.member.dto.response.MemberSignInResponseDTO;
import com.echomap.cherryblossomclean.member.dto.response.MemberSignUpResponseDTO;
import com.echomap.cherryblossomclean.member.service.MemberService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

  private final MemberService memberService;

  @PostMapping()
  public ResponseEntity<?> register(
      @Validated @RequestBody MemberSignUpRequsetDTO dto, BindingResult result) {

    log.info("member POST!! - {}", dto);

    if (result.hasErrors()) {
      log.warn(result.toString());
      return ResponseEntity.badRequest().body(result.getFieldError());
    }

    try {
      MemberSignUpResponseDTO responseDTO = memberService.create(dto);
      return ResponseEntity.ok().body(responseDTO);
    } catch (Exception e) {
      log.warn(e.getMessage());
    }

    return null;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Validated @RequestBody MemberSignInRequestDTO dto) {
    log.info("로그인 접근함");
    try {
      MemberSignInResponseDTO responseDTO = memberService.authenticate(dto);
      return ResponseEntity.ok().body(responseDTO);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }

  @GetMapping("check")
  public ResponseEntity<?> checkEmail(String email) {
    try {
      boolean flag = memberService.isDuplicateEmail(email);
      log.warn("{} 중복여부 - {}", email, flag);
      return ResponseEntity.ok().body(flag);
    } catch (ValidateEmailException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping()
  public ResponseEntity<?> modify(
      @AuthenticationPrincipal TokenUserInfo userInfo,
      @RequestBody MemberModifyRequestDTO dto,
      BindingResult result) {
    return ResponseEntity.ok().build();
  }

  @DeleteMapping()
  public ResponseEntity<?> deleteMember(
      @AuthenticationPrincipal TokenUserInfo userInfo, String password) {
    return ResponseEntity.ok().build();
  }
}
