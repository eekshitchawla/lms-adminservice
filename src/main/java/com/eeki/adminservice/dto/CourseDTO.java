package com.eeki.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private Integer estimatedHours;
    private String instructorName;
}
