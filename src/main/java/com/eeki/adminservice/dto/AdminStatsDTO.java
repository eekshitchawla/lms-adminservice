package com.eeki.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsDTO {
    private Long totalUsers;
    private Long activeUsers;
    private Map<String, Long> usersByRole;
    private Long totalCoursesAssigned;
    private Long completedCourses;
    private Double averageProgress;
    private LocalDateTime lastUpdated;
}

