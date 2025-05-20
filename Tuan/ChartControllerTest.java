package com.thanhtam.backend;

import com.thanhtam.backend.controller.ChartController;
import com.thanhtam.backend.dto.CourseChart;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.service.CourseService;
import com.thanhtam.backend.service.ExamUserService;
import com.thanhtam.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChartControllerTest {

    @Mock
    private CourseService courseService;

    @Mock
    private UserService userService;

    @Mock
    private ExamUserService examUserService;

    @InjectMocks
    private ChartController controller;

    private final String USERNAME = "user1";
    private User user;

    @BeforeEach
    void setUp() {
        // prepare user with intake
        user = new User();
        Intake intake = new Intake(); intake.setId(10L);
        user.setIntake(intake);
        when(userService.getUserName()).thenReturn(USERNAME);
        when(userService.getUserByUsername(USERNAME)).thenReturn(java.util.Optional.of(user));
    }

    private ExamUser exam(double points, int weeksAgo) {
        ExamUser eu = new ExamUser();
        eu.setTotalPoint(points);
        Date finish = new org.joda.time.DateTime().minusWeeks(weeksAgo).toDate();
        eu.setTimeFinish(finish);
        return eu;
    }

    @Test
    @DisplayName("4.1 - both current & last week exams: else branch, zero rate")
    void bothCurrentAndLastWeek_zeroRate() {
        Course c = new Course(1L, "C1", "CODE", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.singletonList(c));
        List<ExamUser> exams = Arrays.asList(exam(5.0, 0), exam(3.0, 1));
        when(examUserService.getCompleteExams(1L, USERNAME)).thenReturn(exams);

        List<CourseChart> charts = controller.getCourseChart();

        assertEquals(1, charts.size());
        CourseChart ch = charts.get(0);
        assertEquals(2, ch.getCountExam());
        assertEquals((5.0+3.0)/2, ch.getTotalPoint());
        assertEquals(0.0, ch.getChangeRating());
        assertEquals(0, ch.getCompareLastWeek());
    }

    @Test
    @DisplayName("4.2 - only current week exams: first if branch")
    void onlyCurrentWeek() {
        Course c = new Course(2L, "C2", "CODE", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.singletonList(c));
        List<ExamUser> exams = Arrays.asList(exam(4.0, 0), exam(6.0, 0));
        when(examUserService.getCompleteExams(2L, USERNAME)).thenReturn(exams);

        CourseChart ch = controller.getCourseChart().get(0);
        assertEquals(2, ch.getCountExam());
        assertEquals((4.0+6.0)/2, ch.getTotalPoint());
        assertEquals(200.0, ch.getChangeRating());
        assertEquals(1, ch.getCompareLastWeek());
    }

    @Test
    @DisplayName("4.3 - only last week exams: third if branch")
    void onlyLastWeek() {
        Course c = new Course(3L, "C3", "CODE", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.singletonList(c));
        List<ExamUser> exams = Arrays.asList(exam(2.0, 1), exam(8.0, 1));
        when(examUserService.getCompleteExams(3L, USERNAME)).thenReturn(exams);

        CourseChart ch = controller.getCourseChart().get(0);
        assertEquals(2, ch.getCountExam());
        assertEquals((2.0+8.0)/2, ch.getTotalPoint());
        assertEquals(200.0, ch.getChangeRating());
        assertEquals(-1, ch.getCompareLastWeek());
    }

    @Test
    @DisplayName("4.4 - no exams in current or last week: second if branch")
    void noneCurrentOrLast() {
        Course c = new Course(4L, "C4", "CODE", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.singletonList(c));
        List<ExamUser> exams = Arrays.asList(exam(1.0, 2), exam(3.0, 3));
        when(examUserService.getCompleteExams(4L, USERNAME)).thenReturn(exams);

        CourseChart ch = controller.getCourseChart().get(0);
        assertEquals(2, ch.getCountExam());
        assertEquals((1.0+3.0)/2, ch.getTotalPoint());
        assertEquals(0.0, ch.getChangeRating());
        assertEquals(0, ch.getCompareLastWeek());
    }

    @Test
    @DisplayName("4.5 - more current than last week: positive rate")
    void moreCurrentExams() {
        Course c = new Course(5L, "C5", "CODE", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.singletonList(c));
        List<ExamUser> exams = Arrays.asList(exam(1.0,0), exam(2.0,0), exam(3.0,0), exam(4.0,1));
        when(examUserService.getCompleteExams(5L, USERNAME)).thenReturn(exams);

        CourseChart ch = controller.getCourseChart().get(0);
        assertEquals(4, ch.getCountExam());
        assertEquals((1+2+3+4)/4.0, ch.getTotalPoint());
        assertEquals(200.0, ch.getChangeRating());
        assertEquals(1, ch.getCompareLastWeek());
    }

    @Test
    @DisplayName("4.6 - more last week than current: negative rate")
    void moreLastExams() {
        Course c = new Course(6L, "C6", "CODE", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.singletonList(c));
        List<ExamUser> exams = Arrays.asList(exam(1.0,1), exam(2.0,1), exam(3.0,1), exam(4.0,0));
        when(examUserService.getCompleteExams(6L, USERNAME)).thenReturn(exams);

        CourseChart ch = controller.getCourseChart().get(0);
        assertEquals(4, ch.getCountExam());
        assertEquals((1+2+3+4)/4.0, ch.getTotalPoint());
        // (-2/3)*100 ≈ -66.67
        assertEquals(-66.67, ch.getChangeRating());
        assertEquals(-1, ch.getCompareLastWeek());
    }

    @Test
    @DisplayName("4.7 - equal current and last week exams: zero rate")
    void equalNumberExams() {
        Course c = new Course(7L, "C7", "CODE", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.singletonList(c));
        List<ExamUser> exams = Arrays.asList(exam(1.0,0), exam(2.0,0), exam(3.0,1), exam(4.0,1));
        when(examUserService.getCompleteExams(7L, USERNAME)).thenReturn(exams);

        CourseChart ch = controller.getCourseChart().get(0);
        assertEquals(4, ch.getCountExam());
        assertEquals((1+2+3+4)/4.0, ch.getTotalPoint());
        assertEquals(0.0, ch.getChangeRating());
        assertEquals(0, ch.getCompareLastWeek());
    }

    @Test
    @DisplayName("4.8 - no exams at all: should throw ArithmeticException")
    void noExamsAll() {
        Course c = new Course(8L, "C8", "CODE", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.singletonList(c));
        when(examUserService.getCompleteExams(8L, USERNAME)).thenReturn(Collections.emptyList());

        assertThrows(ArithmeticException.class, () -> controller.getCourseChart());
    }

    @Test
    @DisplayName("4.9 - unrelated weeks are excluded")
    void mixedWithUnrelatedWeeks() {
        Course c = new Course(9L, "C9", "CODE", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.singletonList(c));
        List<ExamUser> exams = Arrays.asList(exam(5.0,0), exam(3.0,1), exam(7.0,3));
        when(examUserService.getCompleteExams(9L, USERNAME)).thenReturn(exams);

        CourseChart ch = controller.getCourseChart().get(0);
        assertEquals(3, ch.getCountExam());
        assertEquals((5+3+7)/3.0, ch.getTotalPoint());
        assertEquals(0.0, ch.getChangeRating());
        assertEquals(0, ch.getCompareLastWeek());
    }

    @Test
    @DisplayName("4.10 - one exam only this week: single item case")
    void oneExamOnlyCurrent() {
        Course c = new Course(10L, "C10", "CODE", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.singletonList(c));
        when(examUserService.getCompleteExams(10L, USERNAME)).thenReturn(Collections.singletonList(exam(9.0,0)));

        CourseChart ch = controller.getCourseChart().get(0);
        assertEquals(1, ch.getCountExam());
        assertEquals(9.0, ch.getTotalPoint());
        assertEquals(100.0, ch.getChangeRating());
        assertEquals(1, ch.getCompareLastWeek());
    }

    @Test
    @DisplayName("4.11 - exception when user not found")
    void userNotFoundThrows() {
        when(userService.getUserByUsername(USERNAME)).thenReturn(java.util.Optional.empty());
        assertThrows(NoSuchElementException.class, () -> controller.getCourseChart());
    }

    @Test
    @DisplayName("4.12 - empty course list returns empty chart list")
    void emptyCourseList() {
        when(courseService.findAllByIntakeId(10L)).thenReturn(Collections.emptyList());
        List<CourseChart> charts = controller.getCourseChart();
        assertTrue(charts.isEmpty());
    }

    @Test
    @DisplayName("4.13 - multiple courses aggregated separately")
    void multipleCoursesAggregation() {
        Course c1 = new Course(11L, "C11", "CODE1", USERNAME, null);
        Course c2 = new Course(12L, "C12", "CODE2", USERNAME, null);
        when(courseService.findAllByIntakeId(10L)).thenReturn(Arrays.asList(c1, c2));
        when(examUserService.getCompleteExams(11L, USERNAME)).thenReturn(Arrays.asList(exam(5.0,0)));
        when(examUserService.getCompleteExams(12L, USERNAME)).thenReturn(Arrays.asList(exam(6.0,1), exam(4.0,1)));

        List<CourseChart> charts = controller.getCourseChart();
        assertEquals(2, charts.size());
        CourseChart ch1 = charts.stream().filter(ch -> "CODE1".equals(ch.getCourseCode())).findFirst().get();
        assertEquals(1, ch1.getCountExam());
        assertEquals(5.0, ch1.getTotalPoint());
        assertEquals(100.0, ch1.getChangeRating());
        assertEquals(1, ch1.getCompareLastWeek());

        CourseChart ch2 = charts.stream().filter(ch -> "CODE2".equals(ch.getCourseCode())).findFirst().get();
        assertEquals(2, ch2.getCountExam());
        assertEquals((6.0+4.0)/2, ch2.getTotalPoint());
        assertEquals(200.0, ch2.getChangeRating());
        assertEquals(-1, ch2.getCompareLastWeek());
    }
}
