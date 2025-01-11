package com.polytech.CineTour;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.polytech.crud.entity.Location;
import com.polytech.crud.entity.Movie;
import com.polytech.crud.repository.LocationRepository;
import com.polytech.crud.repository.MovieRepository;
import com.polytech.crud.service.ImdbLocationsService;
import com.polytech.crud.service.ImdbMoviesService;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "selenium.remote.url=mock://url",
        "selenium.driver.type=mock"
})
class CineTourApplicationTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ImdbLocationsService imdbLocationsService() {
            return mock(ImdbLocationsService.class);
        }

        @Bean
        public ImdbMoviesService imdbMoviesService() {
            return mock(ImdbMoviesService.class);
        }
    }

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private LocationRepository locationRepository;

    @BeforeEach
    void cleanup() {
        movieRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveMovie() {
        Movie movie = new Movie();
        movie.setTitle("The Shawshank Redemption");
        movie.setIdImdb("tt0111161");

        movieRepository.save(movie);
        Movie foundMovie = movieRepository.findByIdImdb("tt0111161");

        assertThat(foundMovie).isNotNull();
        assertThat(foundMovie.getTitle()).isEqualTo("The Shawshank Redemption");
    }

    @Test
    void shouldUpdateMovie() {
        Movie movie = new Movie();
        movie.setTitle("Original Title");
        movie.setIdImdb("tt0111161");
        movieRepository.save(movie);

        Movie foundMovie = movieRepository.findByIdImdb("tt0111161");
        foundMovie.setTitle("Updated Title");
        movieRepository.save(foundMovie);

        Movie updatedMovie = movieRepository.findByIdImdb("tt0111161");
        assertThat(updatedMovie.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void shouldSaveAndRetrieveLocation() {
        Location location = new Location();
        location.setIdImdb("tt0111161");
        location.setLocationString("Ohio State Reformatory, Mansfield, Ohio, USA");

        locationRepository.save(location);
        List<Location> foundLocations = locationRepository.findByIdImdb("tt0111161");

        assertThat(foundLocations).hasSize(1);
        assertThat(foundLocations.get(0).getLocationString())
                .isEqualTo("Ohio State Reformatory, Mansfield, Ohio, USA");
    }
}