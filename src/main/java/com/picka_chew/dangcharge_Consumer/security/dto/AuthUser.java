package com.picka_chew.dangcharge_Consumer.security.dto;

/** 요청을 보낸 로그인 사용자. producer가 발급한 액세스 토큰의 subject다 */
public record AuthUser(Long userId) {
}
