package com.eeki.adminservice.service;

import com.eeki.adminservice.dto.CourseDTO;
import com.eeki.adminservice.entity.Course;
import com.eeki.adminservice.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        return mapToDTO(course);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByCategory(String category) {
        return courseRepository.findByCategory(category)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByDifficulty(String difficulty) {
        return courseRepository.findByDifficulty(difficulty)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CourseDTO mapToDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .difficulty(course.getDifficulty())
                .estimatedHours(course.getEstimatedHours())
                .instructorName(course.getInstructorName())
                .build();
    }
}
