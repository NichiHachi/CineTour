package com.polytech.crud.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenreCount {
    private String title;
    private Long genreCount;
}
