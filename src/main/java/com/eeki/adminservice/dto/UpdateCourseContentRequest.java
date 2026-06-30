package com.eeki.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCourseContentRequest {

    private Long courseId;

    private String title;
    private String description;
    private String category;
    private Integer estimatedHours;
}
