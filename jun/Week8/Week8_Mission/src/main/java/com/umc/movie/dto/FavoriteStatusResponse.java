package com.umc.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 즐겨찾기하지 않은 것도 정상 상태이므로 404가 아니라 200 + isFavorite:false로 응답한다.
 * is로 시작하는 boolean은 Jackson이 이름을 "favorite"로 바꿀 수 있어 JSON 이름을 고정한다.
 */
public record FavoriteStatusResponse(
        @JsonProperty("isFavorite") boolean isFavorite
) {}
