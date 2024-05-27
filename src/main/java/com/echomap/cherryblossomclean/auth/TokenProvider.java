package com.echomap.cherryblossomclean.auth;

import com.echomap.cherryblossomclean.exception.TokenExpiredException;
import com.echomap.cherryblossomclean.exception.TokenForgedException;
import com.echomap.cherryblossomclean.exception.TokenInvalidException;
import com.echomap.cherryblossomclean.member.entity.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TokenProvider {

  @Value("${jwt.secret}")
  private String SECRET_KEY;

  public String createToken(Member memberEntity, boolean autoLogin) {

    Date expiry;
    if (autoLogin) {
      expiry = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));
    } else {
      expiry = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));
    }

    Map<String, Object> claims = new HashMap<>();
    claims.put("email", memberEntity.getEmail());
    claims.put("role", memberEntity.getRole());

    return Jwts.builder()
        .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS512)
        .setClaims(claims)
        .setHeaderParam("typ", "JWT")
        .setIssuer("운영자")
        .setIssuedAt(new Date())
        .setExpiration(expiry)
        .setSubject(memberEntity.getEmail())
        .compact();
  }

  public TokenUserInfo validateAndGetTokenUserInfo(String token) throws TokenExpiredException, TokenInvalidException, TokenForgedException {
    try {
      Jws<Claims> claimsJws = Jwts.parserBuilder()
              .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
              .build()
              .parseClaimsJws(token);

      Claims claims = claimsJws.getBody();

      // 헤더 검증
      JwsHeader header = claimsJws.getHeader();
      if (header == null
              || !SignatureAlgorithm.HS512.getValue().equals(header.getAlgorithm())
              || !header.getType().equals("JWT")) {
        throw new TokenForgedException("헤더가 위조되었습니다.");
      }

      // 페이로드 검증
      String email = claims.get("email", String.class);
      String roleString = claims.get("role", String.class);

      if (email == null || roleString == null) {
        throw new TokenForgedException("데이터가 위조되었습니다.");
      }

      try {
        Member.Role role = Member.Role.valueOf(roleString);
        return TokenUserInfo.builder()
                .email(email)
                .role(role)
                .build();
      } catch (IllegalArgumentException ex) {
        throw new TokenForgedException("데이터가 위조되었습니다.", ex);
      }
    } catch (io.jsonwebtoken.security.SignatureException ex) {
      throw new TokenForgedException("서명이 위조되었습니다.", ex);
    } catch (MalformedJwtException ex) {
      throw new TokenInvalidException("Malformed token", ex);
    } catch (ExpiredJwtException ex) {
      throw new TokenExpiredException("토큰이 만료되었습니다.", ex);
    } catch (Exception ex) {
      throw new TokenInvalidException("Invalid token", ex);
    }
  }
}
