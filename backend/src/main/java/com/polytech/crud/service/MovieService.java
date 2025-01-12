package com.polytech.crud.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.polytech.crud.entity.Movie;
import com.polytech.crud.repository.MovieRepository;

@Service
public class MovieService {
    @Autowired
    private MovieRepository repository;

    public Movie saveMovie(Movie movie) {
        return repository.save(movie);
    }

    public List<Movie> saveMovies(List<Movie> movies) {
        return repository.saveAll(movies);
    }

    public List<Movie> getAllMovies() {
        return repository.findAll();
    }

    public Movie getMovieById(int id) {
        return repository.findById(id).orElse(null);
    }

    public Movie getMovieByImdbId(String id) {
        return repository.findByIdImdb(id);
    }

    public List<Movie> getMoviesByTitle(String name) {
        return repository.findByTitle(name);
    }

    public String deleteMovieById(int id) {
        try {
            repository.deleteById(id);
            return "Movie removed with the id : " + id;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public Movie updateMovie(Movie movie) {
        Movie existingMovie = repository.findById(movie.getId()).orElse(null);
        existingMovie.setTitle(movie.getTitle());
        return repository.save(existingMovie);
    }
}