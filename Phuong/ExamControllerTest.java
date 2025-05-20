
package com.thanhtam.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.QuestionExamReport;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.service.ExamService;
import com.thanhtam.backend.service.ExamUserService;
import com.thanhtam.backend.service.QuestionService;
import com.thanhtam.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExamControllerTest {

    @InjectMocks private com.thanhtam.backend.controller.ExamController examController;
    @Mock private ExamService examService;
    @Mock private QuestionService questionService;
    @Mock private UserService userService;
    @Mock private ExamUserService examUserService;
    @Mock private ObjectMapper objectMapper;

    @Autowired private MockMvc mockMvc;
    @Autowired private ExamRepository examRepository;
    @Autowired private ExamUserRepository examUserRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =====================================================================================
      //  TEST CASE - Đơn vị: Báo cáo câu hỏi từ AnswerSheet
    @Test
    void testQuestionReportBuildLogicFromAnswerSheetJson() throws Exception {
        Long examId = 10L;

        // Mock exam
        Exam exam = new Exam();
        exam.setId(examId);
        when(examService.getExamById(examId)).thenReturn(Optional.of(exam));

        // Mock examUser with JSON answerSheet
        ExamUser examUser = new ExamUser();
        examUser.setAnswerSheet("[{\"questionId\":1,\"choices\":[0],\"point\":1}]");
        when(examUserService.findAllByExam_Id(examId)).thenReturn(List.of(examUser));

        // Prepare valid AnswerSheet
        AnswerSheet sheet = new AnswerSheet();
        sheet.setQuestionId(1L);
        Choice choice = new Choice(); choice.setId(1L); choice.setIsCorrected(0);
        sheet.setChoices(List.of(choice));
        sheet.setPoint(1);

        List<AnswerSheet> fakeSheet = List.of(sheet);

        //  FIX mock readValue with correct matcher
        when(objectMapper.readValue(anyString(), ArgumentMatchers.<TypeReference<List<AnswerSheet>>>any()))
            .thenReturn(fakeSheet);

        // Mock question
        Question question = new Question();
        question.setId(1L);
        question.setQuestionText("Câu hỏi demo");
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));

        // Run test
        ResponseEntity<List<QuestionExamReport>> response = examController.getResultExamQuestionsReport(examId);

        assertEquals(200, response.getStatusCodeValue());
        List<QuestionExamReport> result = response.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Câu hỏi demo", result.get(0).getQuestion().getQuestionText());
        assertEquals(1, result.get(0).getCorrectTotal());
    }

    // =====================================================================================
    //  TEST CASE 2 - INTEGRATION TEST: Gửi bài làm (PUT)
    @Test
    @WithMockUser(username = "user1")
    void testSubmitAnswerSheet_Integration() throws Exception {
        Long examId = 1L;
        String answerJson = "[{\"questionId\":1,\"choices\":[0,1],\"point\":2}]";

        MvcResult result = mockMvc.perform(put("/exams/" + examId + "/questions-by-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(answerJson))
            .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 200 || status == 404);
    }

    // =====================================================================================
    //  TEST CASE 3 - INTEGRATION TEST: Xem kết quả tổng hợp
    @Test
    @WithMockUser(username = "user1")
    void testGetResultAll_Integration() throws Exception {
        MvcResult result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/exams/1/result/all")
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 200 || status == 404);
    }

    // =====================================================================================
    //  TEST CASE 4 - UNIT TEST: Bài thi không tồn tại
    @Test
    void testExamNotFound() throws Exception {
        Long examId = 404L;
        when(examService.getExamById(examId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = examController.getAllQuestions(examId);
        assertEquals(404, response.getStatusCodeValue());
    }

    // =====================================================================================
    //  TEST CASE 5 - UNIT TEST: Bài thi bị khóa
    @Test
    void testExamLocked_returnsBadRequest() throws Exception {
        Long examId = 10L;
        Exam exam = new Exam();
        exam.setId(examId);
        exam.setLocked(true);
        exam.setBeginExam(new Date(System.currentTimeMillis() - 10000));

        when(examService.getExamById(examId)).thenReturn(Optional.of(exam));

        ResponseEntity<?> response = examController.getAllQuestions(examId);
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("khoá"));
    }
}
