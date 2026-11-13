package com.umc.movie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.LoginRequest;
import com.umc.movie.dto.LoginResponse;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.service.AuthService;
import com.umc.movie.service.MemberService;
import com.umc.movie.service.RatingService;
import com.umc.movie.util.JwtUtil;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class MovieApplicationTests {

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final RatingRepository ratingRepository = mock(RatingRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void nicknameAndEmailAvailability() {
        when(memberRepository.existsByNickname("수현")).thenReturn(true);
        when(memberRepository.existsByEmail("new@umc.com")).thenReturn(false);
        MemberService service = new MemberService(memberRepository, passwordEncoder);

        assertThat(service.checkNickname("수현").available()).isFalse();
        assertThat(service.checkEmail("new@umc.com").available()).isTrue();
    }

    @Test
    void signUpHashesPasswordAndReturnsPublicFields() {
        SignUpRequest request = new SignUpRequest("new@umc.com", "Umc!1234", "새회원");
        Member savedMember = mock(Member.class);
        when(passwordEncoder.encode("Umc!1234")).thenReturn("hashed-password");
        when(memberRepository.save(org.mockito.ArgumentMatchers.any(Member.class)))
                .thenReturn(savedMember);
        when(savedMember.getMemberId()).thenReturn(3L);
        when(savedMember.getEmail()).thenReturn("new@umc.com");
        when(savedMember.getNickname()).thenReturn("새회원");
        MemberService service = new MemberService(memberRepository, passwordEncoder);

        MemberResponse response = service.signUp(request);

        assertThat(response.memberId()).isEqualTo(3L);
        assertThat(response.email()).isEqualTo("new@umc.com");
        assertThat(response.nickname()).isEqualTo("새회원");
        verify(passwordEncoder).encode("Umc!1234");
    }

    @Test
    void memberRatings() {
        Rating first = mock(Rating.class);
        Rating second = mock(Rating.class);
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(1L))
                .thenReturn(List.of(first, second));
        RatingService service = new RatingService(ratingRepository, memberRepository);

        assertThat(service.getRatingsByMember(1L)).hasSize(2);
    }

    @Test
    void unknownMember() {
        when(memberRepository.existsById(999L)).thenReturn(false);
        RatingService service = new RatingService(ratingRepository, memberRepository);

        assertThatThrownBy(() -> service.getRatingsByMember(999L))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void createUpdateAndDeleteRating() {
        Member member = mock(Member.class);
        Rating savedRating = mock(Rating.class);
        Rating ownedRating = mock(Rating.class);
        when(memberRepository.findById(1L)).thenReturn(java.util.Optional.of(member));
        when(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 999L)).thenReturn(false);
        when(ratingRepository.save(org.mockito.ArgumentMatchers.any(Rating.class)))
                .thenReturn(savedRating);
        when(savedRating.getRatingId()).thenReturn(4L);
        when(ratingRepository.findById(4L)).thenReturn(java.util.Optional.of(ownedRating));
        when(ownedRating.getMember()).thenReturn(member);
        when(member.getMemberId()).thenReturn(1L);
        RatingService service = new RatingService(ratingRepository, memberRepository);

        RatingResponse response = service.create(
                1L,
                new CreateRatingRequest(999L, 5, "좋았음")
        );
        service.update(1L, 4L, new UpdateRatingRequest(4, "수정함"));
        service.delete(1L, 4L);

        assertThat(response.ratingId()).isEqualTo(4L);
        verify(ownedRating).update(4, "수정함");
        verify(ratingRepository).delete(ownedRating);
    }

    @Test
    void rejectsOtherMembersRating() {
        Member owner = mock(Member.class);
        Rating rating = mock(Rating.class);
        when(ratingRepository.findById(1L)).thenReturn(java.util.Optional.of(rating));
        when(rating.getMember()).thenReturn(owner);
        when(owner.getMemberId()).thenReturn(1L);
        RatingService service = new RatingService(ratingRepository, memberRepository);

        assertThatThrownBy(() -> service.update(2L, 1L, new UpdateRatingRequest(3, null)))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN)
                );
    }

    @Test
    void loginReturnsAccessToken() {
        Member member = mock(Member.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        when(memberRepository.findByEmail("login@umc.com"))
                .thenReturn(java.util.Optional.of(member));
        when(member.getPassword()).thenReturn("hashed-password");
        when(member.getMemberId()).thenReturn(1L);
        when(member.getEmail()).thenReturn("login@umc.com");
        when(member.getNickname()).thenReturn("로그인회원");
        when(passwordEncoder.matches("Umc!1234", "hashed-password")).thenReturn(true);
        when(jwtUtil.createAccessToken(1L, "login@umc.com")).thenReturn("access-token");
        AuthService service = new AuthService(memberRepository, passwordEncoder, jwtUtil);

        LoginResponse response = service.login(new LoginRequest("login@umc.com", "Umc!1234"));

        assertThat(response.nickname()).isEqualTo("로그인회원");
        assertThat(response.accessToken()).isEqualTo("access-token");
    }

    @Test
    void jwtRejectsTamperedPayload() {
        JwtUtil jwtUtil = new JwtUtil(
                "umc-11th-movie-rating-secret-key-for-unit-test",
                3600L
        );
        String token = jwtUtil.createAccessToken(7L, "jwt@umc.com");
        String[] parts = token.split("\\.");
        char replacement = parts[1].charAt(0) == 'a' ? 'b' : 'a';
        String tampered = parts[0] + "." + replacement + parts[1].substring(1) + "." + parts[2];

        assertThat(jwtUtil.getMemberId(token)).isEqualTo(7L);
        assertThatThrownBy(() -> jwtUtil.getMemberId(tampered))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo("AUTH401_3")
                );
    }
}
