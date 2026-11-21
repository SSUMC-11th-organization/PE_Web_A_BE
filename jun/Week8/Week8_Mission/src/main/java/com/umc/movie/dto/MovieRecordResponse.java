package com.umc.movie.dto;

/** favorite는 addToFavorites가 false였다면 null이다. */
public record MovieRecordResponse(
        RatingResponse rating,
        FavoriteResponse favorite
) {}
