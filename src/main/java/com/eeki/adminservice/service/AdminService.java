package com.eeki.adminservice.service;

import com.eeki.adminservice.dto.AdminStatsDTO;
import com.eeki.adminservice.dto.CourseAssignmentRequest;
import com.eeki.adminservice.dto.UpdateCourseContentRequest;
import com.eeki.adminservice.entity.AdminStats;
import com.eeki.adminservice.entity.User;
import com.eeki.adminservice.entity.UserRole;
import com.eeki.adminservice.repository.AdminStatsRepository;
import com.eeki.adminservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final AdminStatsRepository adminStatsRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public AdminService(AdminStatsRepository adminStatsRepository,
                        UserRepository userRepository,
                        RestTemplate restTemplate) {
        this.adminStatsRepository = adminStatsRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public String assignCourseToUser(CourseAssignmentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Call Catalog Service to enroll user in course
        try {
            // Use the catalog service's enroll endpoint
            String catalogServiceUrl = "http://localhost:8081/api/v1/courses/" + request.getCourseId() + "/enroll";
            
            // Create enroll request payload
            Map<String, Object> enrollPayload = new HashMap<>();
            enrollPayload.put("userId", request.getUserId());
            
            // Call the catalog service
            restTemplate.postForObject(catalogServiceUrl, enrollPayload, String.class);
            System.out.println("Course assigned to user: " + user.getId() + ", course: " + request.getCourseId());
            
            return "Course assigned successfully";
        } catch (Exception e) {
            System.out.println("Error assigning course: " + e.getMessage());
            throw new RuntimeException("Failed to assign course: " + e.getMessage());
        }
    }

    @Transactional
    public String updateCourseContent(UpdateCourseContentRequest request) {
        try {
            String catalogServiceUrl = "http://localhost:8081/api/v1/courses/" + request.getCourseId();
            // This would be the actual call to the catalog service
            System.out.println("Updating course: " + request.getCourseId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to update course: " + e.getMessage());
        }

        return "Course content updated successfully";
    }

    @Transactional(readOnly = true)
    public AdminStatsDTO getGlobalStats() {
        // Get user statistics
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActive(true);
        
        // Get breakdown by role
        Map<String, Long> usersByRole = new HashMap<>();
        for (UserRole role : UserRole.values()) {
            long count = userRepository.countByRole(role);
            usersByRole.put(role.toString(), count);
        }
        
        // Get course statistics from the LMS (task service)
        long totalCoursesAssigned = 0L;
        long completedCourses = 0L;
        double averageProgress = 0.0;
        
        try {
            // These would be fetched from the LMS/task service
            // For now, we can calculate from the tasks table via RestTemplate
            // or we could add a Tasks repository in this service
            totalCoursesAssigned = getTotalTasksAssigned();
            completedCourses = getCompletedTasks();
            averageProgress = calculateAverageProgress();
        } catch (Exception e) {
            System.out.println("Error fetching task statistics: " + e.getMessage());
        }

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .usersByRole(usersByRole)
                .totalCoursesAssigned(totalCoursesAssigned)
                .completedCourses(completedCourses)
                .averageProgress(averageProgress)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private long getTotalTasksAssigned() {
        try {
            // Call the LMS service to get total tasks
            String response = restTemplate.getForObject("http://localhost:8080/api/v1/tasks", String.class);
            // Parse and count from response
            return 0L; // Placeholder - would be calculated from actual task count
        } catch (Exception e) {
            return 0L;
        }
    }

    private long getCompletedTasks() {
        try {
            // Call the LMS service to get completed tasks
            String response = restTemplate.getForObject("http://localhost:8080/api/v1/tasks", String.class);
            // Parse and filter completed tasks
            return 0L; // Placeholder - would be calculated from actual completed tasks
        } catch (Exception e) {
            return 0L;
        }
    }

    private double calculateAverageProgress() {
        try {
            // Calculate average completion rate across all users
            List<User> allUsers = userRepository.findAll();
            if (allUsers.isEmpty()) {
                return 0.0;
            }
            // Placeholder - would calculate from actual task completion data
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Transactional
    public void updateStats(AdminStats stats) {
        AdminStats existingStats = adminStatsRepository.findAll().stream().findFirst()
                .orElse(new AdminStats());

        existingStats.setTotalUsers(stats.getTotalUsers());
        existingStats.setActiveUsers(stats.getActiveUsers());
        existingStats.setTotalCoursesAssigned(stats.getTotalCoursesAssigned());
        existingStats.setCompletedCourses(stats.getCompletedCourses());
        existingStats.setAverageProgress(stats.getAverageProgress());

        adminStatsRepository.save(existingStats);
    }
}
