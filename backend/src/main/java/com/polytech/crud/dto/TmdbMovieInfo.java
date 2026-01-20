package com.polytech.crud.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbMovieInfo {
    private String backdropPath;
    private String posterPath;
    private String overview;

    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String BACKDROP_SIZE = "w1280";
    private static final String POSTER_SIZE = "w500";

    public String getFullBackdropUrl() {
        if (backdropPath == null || backdropPath.isEmpty()) {
            return null;
        }
        return TMDB_IMAGE_BASE_URL + BACKDROP_SIZE + backdropPath;
    }

    public String getFullPosterUrl() {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        return TMDB_IMAGE_BASE_URL + POSTER_SIZE + posterPath;
    }
}
