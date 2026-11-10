package com.umc.movie.dto;

import com.umc.movie.entity.Member;

/** 회원 응답 DTO. password는 의도적으로 포함하지 않는다. */
public record MemberResponse(
        Long memberId,
        String email,
        String nickname
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getMemberId(), member.getEmail(), member.getNickname());
    }
}
