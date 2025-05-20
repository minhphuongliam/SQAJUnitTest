package com.thanhtam.backend;

import java.util.HashSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.service.*;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    @Autowired
    private IntakeService intakeService;

    @Autowired
    private PartService partService;

    @Autowired
    private ExamUserService examUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateExam_shouldReturn200_andSaveToDatabase() throws Exception {
        User user = userService.getUserByUsername("admin").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("admin");
            newUser.setEmail("admin@example.com");
            Role role = new Role();
            role.setName(ERole.ROLE_ADMIN);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            return userService.createUser(newUser);
        });

        Intake intake = intakeService.findAll().stream().findFirst().orElse(null);
        Part part = partService.getPartListByCourse(null).stream().findFirst().orElse(null);

        Exam exam = new Exam();
        exam.setTitle("Unit Test Exam");
        exam.setBeginExam(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2030-01-01 10:00:00"));
        exam.setFinishExam(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2030-01-01 11:00:00"));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");

        mockMvc.perform(post("/api/exams")
                        .param("intakeId", intake != null ? intake.getId().toString() : "1")
                        .param("partId", part != null ? part.getId().toString() : "1")
                        .param("isShuffle", "false")
                        .param("locked", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exam)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Unit Test Exam")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetExamById_shouldReturn200_whenExists() throws Exception {
        Exam createdExam = new Exam();
        createdExam.setTitle("Existing Exam");
        createdExam.setBeginExam(new Date(System.currentTimeMillis() + 100000));
        createdExam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        createdExam.setDurationExam(60);
        createdExam.setQuestionData("[]");
        createdExam = examService.saveExam(createdExam);

        mockMvc.perform(get("/api/exams/" + createdExam.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Existing Exam")));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetAllByUser_shouldReturn200_andListExams() throws Exception {
        Exam exam = new Exam();
        exam.setTitle("Test Exam");
        exam.setBeginExam(new Date(System.currentTimeMillis() + 100000));
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        Exam savedExam = examService.saveExam(exam);

        User user = userService.getUserByUsername("user1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("user1");
            newUser.setEmail("user1@example.com");
            Role role = new Role();
            role.setName(ERole.ROLE_STUDENT);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            return userService.createUser(newUser);
        });

        ExamUser examUser = new ExamUser();
        examUser.setExam(savedExam);
        examUser.setUser(user);
        examUser.setRemainingTime(60 * 60);
        examUser.setIsStarted(false);
        examUser.setIsFinished(false);
        examUser.setTotalPoint(-1.0);
        examUserService.update(examUser);

        mockMvc.perform(get("/api/exams/list-all-by-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetExamUserByExamId_shouldReturn200_whenExists() throws Exception {
        Exam exam = new Exam();
        exam.setTitle("ExamUser Lookup");
        exam.setBeginExam(new Date(System.currentTimeMillis() - 3600000));
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        Exam savedExam = examService.saveExam(exam);

        User user = userService.getUserByUsername("user1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("user1");
            newUser.setEmail("user1@example.com");
            Role role = new Role();
            role.setName(ERole.ROLE_STUDENT);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            return userService.createUser(newUser);
        });

        ExamUser examUser = new ExamUser();
        examUser.setExam(savedExam);
        examUser.setUser(user);
        examUser.setRemainingTime(60 * 60);
        examUser.setIsStarted(true);
        examUser.setIsFinished(false);
        examUser.setTotalPoint(-1.0);
        examUserService.update(examUser);

        mockMvc.perform(get("/api/exams/exam-user/" + savedExam.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exam.title", is("ExamUser Lookup")));
    }
    
    // ...

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetExamQuestions_shouldReturn400_ifExamIsLockedOrBeforeStart() throws Exception {
        Exam exam = new Exam();
        exam.setTitle("Locked Exam");
        exam.setBeginExam(new Date(System.currentTimeMillis() + 600000)); // future
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        exam.setLocked(true);
        Exam savedExam = examService.saveExam(exam);

        User user = userService.getUserByUsername("user1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("user1");
            newUser.setEmail("user1@example.com");
            Role role = new Role();
            role.setName(ERole.ROLE_STUDENT);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            return userService.createUser(newUser);
        });

        ExamUser examUser = new ExamUser();
        examUser.setExam(savedExam);
        examUser.setUser(user);
        examUser.setRemainingTime(60 * 60);
        examUser.setIsStarted(false);
        examUser.setIsFinished(false);
        examUser.setTotalPoint(-1.0);
        examUserService.update(examUser);

        mockMvc.perform(get("/api/exams/" + savedExam.getId() + "/questions"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Bài thi đang bị khoá")));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetExamQuestions_shouldReturn200_ifValidAndNotLocked() throws Exception {
        Exam exam = new Exam();
        exam.setTitle("Active Exam");
        exam.setBeginExam(new Date(System.currentTimeMillis() - 600000)); // started
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        exam.setLocked(false);
        Exam savedExam = examService.saveExam(exam);

        User user = userService.getUserByUsername("user1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("user1");
            newUser.setEmail("user1@example.com");
            Role role = new Role();
            role.setName(ERole.ROLE_STUDENT);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            return userService.createUser(newUser);
        });

        ExamUser examUser = new ExamUser();
        examUser.setExam(savedExam);
        examUser.setUser(user);
        examUser.setRemainingTime(60 * 60);
        examUser.setIsStarted(false);
        examUser.setIsFinished(false);
        examUser.setTotalPoint(-1.0);
        examUserService.update(examUser);

        mockMvc.perform(get("/api/exams/" + savedExam.getId() + "/questions"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testSaveUserExamAnswer_shouldReturn200_whenValid() throws Exception {
        Exam exam = new Exam();
        exam.setTitle("Submit Exam");
        exam.setBeginExam(new Date(System.currentTimeMillis() - 600000));
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        Exam savedExam = examService.saveExam(exam);

        User user = userService.getUserByUsername("user1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("user1");
            newUser.setEmail("user1@example.com");
            Role role = new Role();
            role.setName(ERole.ROLE_STUDENT);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            return userService.createUser(newUser);
        });

        ExamUser examUser = new ExamUser();
        examUser.setExam(savedExam);
        examUser.setUser(user);
        examUser.setRemainingTime(60 * 60);
        examUser.setIsStarted(true);
        examUser.setIsFinished(false);
        examUser.setTotalPoint(-1.0);
        examUser.setAnswerSheet("[]");
        examUserService.update(examUser);

        String body = "[{\"questionId\": 1, \"choices\": [], \"point\": 10}]";

        mockMvc.perform(put("/api/exams/" + savedExam.getId() + "/questions-by-user")
                        .param("isFinish", "true")
                        .param("remainingTime", "300")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetResultExamAll_shouldReturn200_withValidScores() throws Exception {
        Exam exam = new Exam();
        exam.setTitle("Result Exam");
        exam.setBeginExam(new Date(System.currentTimeMillis() - 600000));
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        Exam savedExam = examService.saveExam(exam);

        User user = userService.getUserByUsername("user1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("user1");
            newUser.setEmail("user1@example.com");
            Role role = new Role();
            role.setName(ERole.ROLE_STUDENT);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            return userService.createUser(newUser);
        });

        ExamUser examUser = new ExamUser();
        examUser.setExam(savedExam);
        examUser.setUser(user);
        examUser.setRemainingTime(60 * 60);
        examUser.setIsStarted(true);
        examUser.setIsFinished(true);
        examUser.setTotalPoint(-1.0);
        examUser.setAnswerSheet("[]");
        examUserService.update(examUser);

        mockMvc.perform(get("/api/exams/" + savedExam.getId() + "/result/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetResultExam_shouldReturn200_forSingleUser() throws Exception {
        Exam exam = new Exam();
        exam.setTitle("My Result");
        exam.setBeginExam(new Date(System.currentTimeMillis() - 600000));
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        Exam savedExam = examService.saveExam(exam);

        User user = userService.getUserByUsername("user1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("user1");
            newUser.setEmail("user1@example.com");
            Role role = new Role();
            role.setName(ERole.ROLE_STUDENT);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            return userService.createUser(newUser);
        });

        ExamUser examUser = new ExamUser();
        examUser.setExam(savedExam);
        examUser.setUser(user);
        examUser.setRemainingTime(3600);
        examUser.setIsStarted(true);
        examUser.setIsFinished(true);
        examUser.setAnswerSheet("[]");
        examUser.setTotalPoint(-1.0);
        examUserService.update(examUser);

        mockMvc.perform(get("/api/exams/" + savedExam.getId() + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exam.title", is("My Result")))
                .andExpect(jsonPath("$.totalPoint", anyOf(nullValue(), isA(Number.class))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetResultExamByUser_shouldReturn200_forGivenUser() throws Exception {
        Exam exam = new Exam();
        exam.setTitle("User Specific Result");
        exam.setBeginExam(new Date(System.currentTimeMillis() - 600000));
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        Exam savedExam = examService.saveExam(exam);

        User user = userService.getUserByUsername("user1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("user1");
            newUser.setEmail("user1@example.com");
            Role role = new Role();
            role.setName(ERole.ROLE_STUDENT);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            return userService.createUser(newUser);
        });

        ExamUser examUser = new ExamUser();
        examUser.setExam(savedExam);
        examUser.setUser(user);
        examUser.setRemainingTime(3500);
        examUser.setIsStarted(true);
        examUser.setIsFinished(true);
        examUser.setAnswerSheet("[]");
        examUser.setTotalPoint(-1.0);
        examUserService.update(examUser);

        mockMvc.perform(get("/api/exams/" + savedExam.getId() + "/users/user1/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exam.title", is("User Specific Result")))
                .andExpect(jsonPath("$.user.username", is("user1")))
                .andExpect(jsonPath("$.remainingTime", greaterThanOrEqualTo(0)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetResultExamQuestionsReport_shouldReturn200_withReport() throws Exception {
        Exam exam = new Exam();
        exam.setTitle("Report Exam");
        exam.setBeginExam(new Date(System.currentTimeMillis() - 600000));
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        Exam savedExam = examService.saveExam(exam);

        User user = userService.getUserByUsername("user1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("user1");
            newUser.setEmail("user1@example.com");
            Role role = new Role();
            role.setName(ERole.ROLE_STUDENT);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            return userService.createUser(newUser);
        });

        ExamUser examUser = new ExamUser();
        examUser.setExam(savedExam);
        examUser.setUser(user);
        examUser.setRemainingTime(3400);
        examUser.setIsStarted(true);
        examUser.setIsFinished(true);
        examUser.setAnswerSheet("[]");
        examUser.setTotalPoint(-1.0);
        examUserService.update(examUser);

        mockMvc.perform(get("/api/exams/" + savedExam.getId() + "/result/all/question-report"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("question")));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetQuestionTextByExamId_shouldReturn200_withList() throws Exception {
        Exam exam = new Exam();
        exam.setTitle("Text Question Exam");
        exam.setBeginExam(new Date(System.currentTimeMillis() - 600000));
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setDurationExam(60);
        exam.setQuestionData("[]");
        Exam savedExam = examService.saveExam(exam);

        mockMvc.perform(get("/api/exam/" + savedExam.getId() + "/question-text"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetExamSchedule_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/exams/schedule"))
                .andExpect(status().isOk());
    }

}
