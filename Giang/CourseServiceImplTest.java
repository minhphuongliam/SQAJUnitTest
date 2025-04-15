package com.thanhtam.backend;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.service.CourseServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    private CourseRepository repo;
    private CourseServiceImpl service;

    private Course makeCourse(Long id, String code) {
        Course c = new Course();
        c.setId(id);
        c.setCourseCode(code);
        return c;
    }

    @BeforeEach
    void setUp() {
        repo = mock(CourseRepository.class);
        service = new CourseServiceImpl(repo);
    }

    // 10.1 Retrieve a course by existing ID
    @Test
    void _10_1_getCourseById_found() {
        Course c = makeCourse(1L, "CS101");
        when(repo.findById(1L)).thenReturn(Optional.of(c));
        assertTrue(service.getCourseById(1L).isPresent());
    }

    // 10.2 Retrieve a course by non-existing ID
    @Test
    void _10_2_getCourseById_notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertFalse(service.getCourseById(99L).isPresent());
    }

    // 10.3 Retrieve a list of all courses (non-empty)
    @Test
    void _10_3_getCourseList_nonEmpty() {
        when(repo.findAll()).thenReturn(List.of(makeCourse(1L, "CS101")));
        assertFalse(service.getCourseList().isEmpty());
    }

    // 10.4 Retrieve a list of all courses (empty)
    @Test
    void _10_4_getCourseList_empty() {
        when(repo.findAll()).thenReturn(Collections.emptyList());
        assertTrue(service.getCourseList().isEmpty());
    }

    // 10.5 Retrieve a page of courses (non-empty)
    @Test
    void _10_5_getCoursePage_nonEmpty() {
        Page<Course> page = new PageImpl<>(List.of(makeCourse(1L, "CS101")));
        when(repo.findAll(any(Pageable.class))).thenReturn(page);
        assertFalse(service.getCourseListByPage(Pageable.unpaged()).isEmpty());
    }

    // 10.6 Retrieve a page of courses (empty)
    @Test
    void _10_6_getCoursePage_empty() {
        when(repo.findAll(any(Pageable.class))).thenReturn(Page.empty());
        assertTrue(service.getCourseListByPage(Pageable.unpaged()).isEmpty());
    }

    // 10.7 Save a valid course
    @Test
    void _10_7_saveCourse() {
        Course c = makeCourse(2L, "CS102");
        service.saveCourse(c);
        verify(repo).save(c);
    }

    // 10.8 Delete a course by existing ID
    @Test
    void _10_8_deleteCourse_existingId() {
        doNothing().when(repo).deleteById(1L);
        service.delete(1L);
        verify(repo).deleteById(1L);
    }

    // 10.9 Delete a course by non-existing ID (still calls repo)
    @Test
    void _10_9_deleteCourse_nonExistingId() {
        doNothing().when(repo).deleteById(99L);
        service.delete(99L);
        verify(repo).deleteById(99L);
    }

    // 10.10 Check existence by course code (existing)
    @Test
    void _10_10_existsByCode_existing() {
        when(repo.existsByCourseCode("CS101")).thenReturn(true);
        assertTrue(service.existsByCode("CS101"));
    }

    // 10.11 Check existence by course code (non-existing)
    @Test
    void _10_11_existsByCode_nonExisting() {
        when(repo.existsByCourseCode("CS999")).thenReturn(false);
        assertFalse(service.existsByCode("CS999"));
    }

    // 10.12 Check existence by ID (existing)
    @Test
    void _10_12_existsById_existing() {
        when(repo.existsById(1L)).thenReturn(true);
        assertTrue(service.existsById(1L));
    }

    // 10.13 Check existence by ID (non-existing)
    @Test
    void _10_13_existsById_nonExisting() {
        when(repo.existsById(5L)).thenReturn(false);
        assertFalse(service.existsById(5L));
    }

    // 10.14 Find all courses by intakeId (non-empty)
    @Test
    void _10_14_findAllByIntakeId_nonEmpty() {
        when(repo.findAllByIntakeId(1L)).thenReturn(List.of(makeCourse(1L, "CS101")));
        assertFalse(service.findAllByIntakeId(1L).isEmpty());
    }

    // 10.15 Find all courses by intakeId (empty)
    @Test
    void _10_15_findAllByIntakeId_empty() {
        when(repo.findAllByIntakeId(2L)).thenReturn(Collections.emptyList());
        assertTrue(service.findAllByIntakeId(2L).isEmpty());
    }

    // 10.16 Find course by partId (found)
    @Test
    void _10_16_findCourseByPartId_found() {
        Course c = makeCourse(3L, "CS103");
        when(repo.findCourseByPartId(10L)).thenReturn(c);
        assertNotNull(service.findCourseByPartId(10L));
    }

    // 10.17 Find course by partId (not found / null)
    @Test
    void _10_17_findCourseByPartId_notFound() {
        when(repo.findCourseByPartId(11L)).thenReturn(null);
        assertNull(service.findCourseByPartId(11L));
    }
}
