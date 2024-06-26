package com.diegojacober.hungryhubauth.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.diegojacober.hungryhubauth.config.security.AccessToken.AllowedRoles;
import com.diegojacober.hungryhubauth.entities.Movie;

@RestController
public class MovieController {

    Map<Long, Movie> movies;

    public MovieController() {
        movies = Map.of(
                1L, new Movie("Star Wars: A New Hope", "George Lucas", 1977),
                2L, new Movie("Star Wars: The Empire Strikes Back", "George Lucas", 1980),
                3L, new Movie("Star Wars: Return of the Jedi", "George Lucas", 1983));
    }

    @GetMapping("/movies")
    // @AllowedRoles("admin")
    public List<Movie> getAllMovies(){
        return new ArrayList<>(movies.values());
    }

    @GetMapping("/movies/{id}")
    @AllowedRoles("instructor")
    public Movie getMovieById(@PathVariable("id") String id){
        return movies.get(Long.valueOf(id));
    }
}
