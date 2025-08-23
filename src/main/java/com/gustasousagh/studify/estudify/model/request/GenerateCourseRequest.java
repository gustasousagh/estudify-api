package com.gustasousagh.studify.estudify.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCourseRequest {

    @NotBlank(message = "A descrição do curso não pode ficar vazia")
    private String courseDescription;

    @NotBlank(message = "O título do curso não pode ficar vazio")
    private Integer courseNumberOfModules;

    @NotBlank(message = "O título do curso não pode ficar vazio")
    private String courseTitle;

}