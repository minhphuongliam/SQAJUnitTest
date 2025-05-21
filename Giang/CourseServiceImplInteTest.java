package com.thanhtam.backend;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class CourseServiceImplInteTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    private String generateCourseCode(String base) {
        return base + "_" + UUID.randomUUID().toString().substring(0, 6);
    }

    // @Test
    // void _12_7_saveCourse() {
    //     String code = generateCourseCode("CS202");

    //     Course course = new Course();
    //     course.setCourseCode(code);
    //     course.setCourseName("Database");

    //     courseService.saveCourse(course);

    //     List<Course> allCourses = courseRepository.findAll();
    //     boolean exists = allCourses.stream().anyMatch(c -> code.equals(c.getCourseCode()));
    //     assertTrue(exists);
    // }

    @Test
    void _12_8_deleteCourse_existingId() {
        String code = generateCourseCode("DEL101");

        Course course = new Course();
        course.setCourseCode(code);
        course.setName("Delete Test");
        Course saved = courseRepository.save(course);

        courseService.delete(saved.getId());
        assertFalse(courseRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void _12_9_deleteCourse_nonExistingId() {
        // Should not throw error when ID doesn't exist
        courseService.delete(99999L);
    }
}
