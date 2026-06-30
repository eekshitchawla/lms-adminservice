package com.eeki.adminservice.controller;

import com.eeki.adminservice.dto.AdminStatsDTO;
import com.eeki.adminservice.dto.CourseAssignmentRequest;
import com.eeki.adminservice.dto.UpdateCourseContentRequest;
import com.eeki.adminservice.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/courses/assign")
    public ResponseEntity<String> assignCourseToUser(@Valid @RequestBody CourseAssignmentRequest request) {
        String response = adminService.assignCourseToUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminStatsDTO> getGlobalStats() {
        AdminStatsDTO stats = adminService.getGlobalStats();
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/courses/{id}/content")
    public ResponseEntity<String> updateCourseContent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseContentRequest request) {

        request.setCourseId(id);
        String response = adminService.updateCourseContent(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Admin service is running");
    }
}
