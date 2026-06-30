package com.eeki.adminservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_stats", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long totalUsers = 0L;

    @Column(nullable = false)
    private Long activeUsers = 0L;

    @Column(nullable = false)
    private Long totalCoursesAssigned = 0L;

    @Column(nullable = false)
    private Long completedCourses = 0L;

    @Column(nullable = false)
    private Double averageProgress = 0.0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
