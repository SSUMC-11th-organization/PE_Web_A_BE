package com.umc.movie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 즐겨찾기는 영화의 속성이 아니라 "회원과 영화의 관계"다.
 * (member_id, movie_id) 복합 UNIQUE로 같은 회원이 같은 영화를 두 번 담는 것을 DB도 막는다.
 */
@Entity
@Table(name = "favorite", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "movie_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;

    // 즐겨찾기의 주인은 로그인한 회원이다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // TMDB의 영화 ID를 그대로 저장한다. 우리 DB에 movie 테이블이 없으므로 외래 키가 아니다.
    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Builder
    public Favorite(Member member, Long movieId) {
        this.member = member;
        this.movieId = movieId;
    }
}
