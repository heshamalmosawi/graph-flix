package com.graphflix.movieservice.dto;

import java.util.Set;

import com.graphflix.movieservice.model.Person;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {

    private String id;
    private String title;
    private Integer released;
    private String tagline;
    private Set<PersonDTO> actors;
    private Set<PersonDTO> directors;
}
