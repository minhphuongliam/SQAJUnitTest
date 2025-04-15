package com.thanhtam.backend;

import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.service.ExamUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamUserServiceImplTest {

    @Mock
    private ExamUserRepository examUserRepository;

    @Mock
    private ExamRepository examRepository;

    @InjectMocks
    private ExamUserServiceImpl examUserService;

    private Exam exam;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        exam = new Exam();
        exam.setId(1L);
        exam.setDurationExam(30);

        user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
    }

    @Test
    @DisplayName("L2_1")
    void createExamUser_L2_1() {
        List<User> userSet = Arrays.asList(user1, user2);
        examUserService.create(exam, userSet);

        List<ExamUser> expectedExamUsers = new ArrayList<>();
        ExamUser examUser1 = new ExamUser();
        examUser1.setUser(user1);
        examUser1.setExam(exam);
        examUser1.setRemainingTime(1800);
        examUser1.setTotalPoint(-1.0);
        expectedExamUsers.add(examUser1);

        ExamUser examUser2 = new ExamUser();
        examUser2.setUser(user2);
        examUser2.setExam(exam);
        examUser2.setRemainingTime(1800);
        examUser2.setTotalPoint(-1.0);
        expectedExamUsers.add(examUser2);

        verify(examUserRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("L2_2")
    void createExamUser_EmptyUserSet_L2_2() {
        List<User> userSet = new ArrayList<>();
        examUserService.create(exam, userSet);
        verify(examUserRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("L2_3")
    void getExamListByUsername_L2_3() {
        String username = "john";
        ExamUser examUser1 = new ExamUser();
        ExamUser examUser2 = new ExamUser();
        List<ExamUser> examUsers = Arrays.asList(examUser1, examUser2);

        when(examUserRepository.findAllByUser_UsernameAndExam_Canceled(username, false)).thenReturn(examUsers);

        List<ExamUser> result = examUserService.getExamListByUsername(username);

        assertEquals(examUsers, result);
        verify(examUserRepository, times(1)).findAllByUser_UsernameAndExam_Canceled(username, false);
    }

    @Test
    @DisplayName("L2_4")
    void findByExamAndUser_L2_4() {
        Long examId = 1L;
        String username = "john";
        ExamUser examUser = new ExamUser();

        when(examUserRepository.findByExam_IdAndUser_Username(examId, username)).thenReturn(examUser);

        ExamUser result = examUserService.findByExamAndUser(examId, username);

        assertEquals(examUser, result);
        verify(examUserRepository, times(1)).findByExam_IdAndUser_Username(examId, username);
    }

    @Test
    @DisplayName("L2_5")
    void updateExamUser_L2_5() {
        ExamUser examUser = new ExamUser();
        examUser.setTotalPoint(10.0);

        examUserService.update(examUser);

        verify(examUserRepository, times(1)).save(examUser);
    }

    @Test
    @DisplayName("L2_6")
    void findExamUserById_Exists_L2_6() {
        Long id = 10L;
        ExamUser examUser = new ExamUser();

        when(examUserRepository.findById(id)).thenReturn(Optional.of(examUser));

        Optional<ExamUser> result = examUserService.findExamUserById(id);

        assertTrue(result.isPresent());
        assertEquals(examUser, result.get());
        verify(examUserRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("L2_7")
    void findExamUserById_NotExists_L2_7() {
        Long id = 999L;

        when(examUserRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ExamUser> result = examUserService.findExamUserById(id);

        assertFalse(result.isPresent());
        verify(examUserRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("L2_8")
    void getCompleteExams_L2_8() {
        Long courseId = 2L;
        String username = "john";
        ExamUser examUser1 = new ExamUser();
        ExamUser examUser2 = new ExamUser();
        List<ExamUser> examUsers = Arrays.asList(examUser1, examUser2);

        when(examUserRepository.findAllByExam_Part_Course_IdAndUser_UsernameAndTotalPointIsGreaterThan(courseId, username, -1.0)).thenReturn(examUsers);

        List<ExamUser> result = examUserService.getCompleteExams(courseId, username);

        assertEquals(examUsers, result);
        verify(examUserRepository, times(1)).findAllByExam_Part_Course_IdAndUser_UsernameAndTotalPointIsGreaterThan(courseId, username, -1.0);
    }

    @Test
    @DisplayName("L2_9")
    void findAllByExamId_L2_9() {
        Long examId = 1L;
        ExamUser examUser1 = new ExamUser();
        ExamUser examUser2 = new ExamUser();
        List<ExamUser> examUsers = Arrays.asList(examUser1, examUser2);

        when(examUserRepository.findAllByExam_Id(examId)).thenReturn(examUsers);

        List<ExamUser> result = examUserService.findAllByExam_Id(examId);

        assertEquals(examUsers, result);
        verify(examUserRepository, times(1)).findAllByExam_Id(examId);
    }

    @Test
    @DisplayName("L2_10")
    void findExamUsersByIsFinishedIsTrueAndExam_Id_L2_10() {
        Long examId = 1L;
        ExamUser examUser1 = new ExamUser();
        ExamUser examUser2 = new ExamUser();
        List<ExamUser> examUsers = Arrays.asList(examUser1, examUser2);

        when(examUserRepository.findExamUsersByIsFinishedIsTrueAndExam_Id(examId)).thenReturn(examUsers);

        List<ExamUser> result = examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(examId);

        assertEquals(examUsers, result);
        verify(examUserRepository, times(1)).findExamUsersByIsFinishedIsTrueAndExam_Id(examId);
    }
}