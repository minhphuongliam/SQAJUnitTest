package com.thanhtam.backend;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ChoiceCorrect;
import com.thanhtam.backend.dto.ChoiceList;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.Choice;
import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.service.ChoiceService;
import com.thanhtam.backend.service.ExamServiceImpl;
import com.thanhtam.backend.service.PartService;
import com.thanhtam.backend.service.QuestionService;
import com.thanhtam.backend.service.UserService;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamServiceImplTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private IntakeRepository intakeRepository;

    @Mock
    private PartService partService;

    @Mock
    private UserService userService;

    @Mock
    private QuestionService questionService;

    @Mock
    private ChoiceService choiceService;

    @InjectMocks
    private ExamServiceImpl examService;

    private Exam exam;

    @BeforeEach
    void setUp() {
        exam = new Exam();
        exam.setId(1L);
        exam.setTitle("Test Exam");
    }

    @Test
    @DisplayName("5_1")
    void saveNewExam_L5_1() {
        Exam newExam = new Exam();
        newExam.setTitle("New Exam");
        when(examRepository.save(newExam)).thenReturn(exam);

        Exam savedExam = examService.saveExam(newExam);

        assertEquals(exam, savedExam);
        verify(examRepository, times(1)).save(newExam);
    }

    @Test
    @DisplayName("L5_2")
    void updateExistingExam_5_2() {
        exam.setTitle("Updated Exam");
        when(examRepository.save(exam)).thenReturn(exam);

        Exam updatedExam = examService.saveExam(exam);

        assertEquals(exam, updatedExam);
        verify(examRepository, times(1)).save(exam);
    }

    @Test
    @DisplayName("5_3")
    void findAllExamsWithPagination_5_3() {
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        List<Exam> exams = Arrays.asList(exam);
        Page<Exam> examPage = new PageImpl<>(exams, pageable, exams.size());

        when(examRepository.findAll(pageable)).thenReturn(examPage);

        Page<Exam> result = examService.findAll(pageable);

        assertEquals(examPage, result);
        verify(examRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("5_4")
    void cancelExam_5_4() {
        Long examId = 1L;
        examService.cancelExam(examId);
        verify(examRepository, times(1)).cancelExam(examId);
    }

    @Test
    @DisplayName("5_5")
    void getAllExams_5_5() {
        List<Exam> exams = Arrays.asList(exam);
        when(examRepository.findAll()).thenReturn(exams);

        List<Exam> result = examService.getAll();

        assertEquals(exams, result);
        verify(examRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("5_6")
    void getExamById_5_6() {
        Long examId = 10L;
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

        Optional<Exam> result = examService.getExamById(examId);

        assertTrue(result.isPresent());
        assertEquals(exam, result.get());
        verify(examRepository, times(1)).findById(examId);
    }

    @Test
    @DisplayName("5_7")
    void getExamByIdNotFound_5_7() {
        Long examId = 13L;
        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        Optional<Exam> result = examService.getExamById(examId);

        assertFalse(result.isPresent());
        verify(examRepository, times(1)).findById(examId);
    }

    @Test
    @DisplayName("5_8")
    void findAllByCreatedByUsername_5_8() {
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        String username = "teacher1";
        List<Exam> exams = Arrays.asList(exam);
        Page<Exam> examPage = new PageImpl<>(exams, pageable, exams.size());

        when(examRepository.findAllByCreatedBy_Username(pageable, username)).thenReturn(examPage);

        Page<Exam> result = examService.findAllByCreatedBy_Username(pageable, username);

        assertEquals(examPage, result);
        verify(examRepository, times(1)).findAllByCreatedBy_Username(pageable, username);
    }

    @Test
    @DisplayName("5_9")
    void getChoiceList_ProcessTFCorrect_5_9() {
        // Arrange
        AnswerSheet userAnswer = new AnswerSheet();
        userAnswer.setQuestionId(1L);
        List<Choice> choices = new ArrayList<>();
        Choice choice = new Choice();
        choice.setId(101L);
        choice.setChoiceText("True");
        choices.add(choice);
        userAnswer.setChoices(choices);
        userAnswer.setPoint(10);

        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(1L);
        examQuestionPoint.setPoint(10);

        List<AnswerSheet> userChoices = Arrays.asList(userAnswer);
        List<ExamQuestionPoint> examQuestionPoints = Arrays.asList(examQuestionPoint);

        Question question = new Question();
        question.setId(1L);
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.TF);
        question.setQuestionType(questionType);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findChoiceTextById(101L)).thenReturn("True");

        // Act
        List<ChoiceList> choiceLists = examService.getChoiceList(userChoices, examQuestionPoints);

        // Assert
        assertNotNull(choiceLists);
        assertEquals(1, choiceLists.size());
        ChoiceList choiceList = choiceLists.get(0);
        assertEquals(question, choiceList.getQuestion());
        assertEquals(10, choiceList.getPoint());
        assertTrue(choiceList.getIsSelectedCorrected());

        List<ChoiceCorrect> choiceCorrects = choiceList.getChoices();
        assertEquals(1, choiceCorrects.size());
        assertEquals(1, choiceCorrects.get(0).getIsRealCorrect());

        verify(questionService, times(1)).getQuestionById(1L);
        verify(choiceService, times(1)).findChoiceTextById(101L);
    }

    @Test
    @DisplayName("5_10")
    void getChoiceList_ProcessTFFail_5_10() {
        // Arrange
        AnswerSheet userAnswer = new AnswerSheet();
        userAnswer.setQuestionId(1L);
        List<Choice> choices = new ArrayList<>();
        Choice choice = new Choice();
        choice.setId(101L);
        choice.setChoiceText("False");
        choices.add(choice);
        userAnswer.setChoices(choices);
        userAnswer.setPoint(10);

        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(1L);
        examQuestionPoint.setPoint(10);

        List<AnswerSheet> userChoices = Arrays.asList(userAnswer);
        List<ExamQuestionPoint> examQuestionPoints = Arrays.asList(examQuestionPoint);

        Question question = new Question();
        question.setId(1L);
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.TF);
        question.setQuestionType(questionType);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findChoiceTextById(101L)).thenReturn("True");

        // Act
        List<ChoiceList> choiceLists = examService.getChoiceList(userChoices, examQuestionPoints);

        // Assert
        assertNotNull(choiceLists);
        assertEquals(1, choiceLists.size());
        ChoiceList choiceList = choiceLists.get(0);
        assertEquals(question, choiceList.getQuestion());
        assertEquals(10, choiceList.getPoint());
        assertFalse(choiceList.getIsSelectedCorrected());

        List<ChoiceCorrect> choiceCorrects = choiceList.getChoices();
        assertEquals(1, choiceCorrects.size());
        assertEquals(0, choiceCorrects.get(0).getIsRealCorrect());

        verify(questionService, times(1)).getQuestionById(1L);
        verify(choiceService, times(1)).findChoiceTextById(101L);
    }

    @Test
    @DisplayName("5_11")
    void getChoiceList_ProcessMCCorrect_5_11() {
        // Arrange
        AnswerSheet userAnswer = new AnswerSheet();
        userAnswer.setQuestionId(1L);
        List<Choice> choices = new ArrayList<>();
        Choice choice = new Choice();
        choice.setId(101L);
        choice.setIsCorrected(1);
        choices.add(choice);
        userAnswer.setChoices(choices);
        userAnswer.setPoint(10);

        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(1L);
        examQuestionPoint.setPoint(10);

        List<AnswerSheet> userChoices = Arrays.asList(userAnswer);
        List<ExamQuestionPoint> examQuestionPoints = Arrays.asList(examQuestionPoint);

        Question question = new Question();
        question.setId(1L);
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MC);
        question.setQuestionType(questionType);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findIsCorrectedById(101L)).thenReturn(1);

        // Act
        List<ChoiceList> choiceLists = examService.getChoiceList(userChoices, examQuestionPoints);

        // Assert
        assertNotNull(choiceLists);
        assertEquals(1, choiceLists.size());
        ChoiceList choiceList = choiceLists.get(0);
        assertEquals(question, choiceList.getQuestion());
        assertEquals(10, choiceList.getPoint());
        assertTrue(choiceList.getIsSelectedCorrected());

        List<ChoiceCorrect> choiceCorrects = choiceList.getChoices();
        assertEquals(1, choiceCorrects.size());
        assertEquals(1, choiceCorrects.get(0).getIsRealCorrect());

        verify(questionService, times(1)).getQuestionById(1L);
        verify(choiceService, times(1)).findIsCorrectedById(101L);
    }

    @Test
    @DisplayName("5_12")
    void getChoiceList_ProcessMCIncorrect_5_12() {
        // Arrange
        AnswerSheet userAnswer = new AnswerSheet();
        userAnswer.setQuestionId(1L);
        List<Choice> choices = new ArrayList<>();
        Choice choice = new Choice();
        choice.setId(101L);
        choice.setIsCorrected(0);
        choices.add(choice);
        userAnswer.setChoices(choices);
        userAnswer.setPoint(10);

        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(1L);
        examQuestionPoint.setPoint(10);

        List<AnswerSheet> userChoices = Arrays.asList(userAnswer);
        List<ExamQuestionPoint> examQuestionPoints = Arrays.asList(examQuestionPoint);

        Question question = new Question();
        question.setId(1L);
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MC);
        question.setQuestionType(questionType);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findIsCorrectedById(101L)).thenReturn(1);

        // Act
        List<ChoiceList> choiceLists = examService.getChoiceList(userChoices, examQuestionPoints);

        // Assert
        assertNotNull(choiceLists);
        assertEquals(1, choiceLists.size());
        ChoiceList choiceList = choiceLists.get(0);
        assertEquals(question, choiceList.getQuestion());
        assertEquals(10, choiceList.getPoint());
        assertFalse(choiceList.getIsSelectedCorrected());

        List<ChoiceCorrect> choiceCorrects = choiceList.getChoices();
        assertEquals(1, choiceCorrects.size());
        assertEquals(1, choiceCorrects.get(0).getIsRealCorrect());

        verify(questionService, times(1)).getQuestionById(1L);
        verify(choiceService, times(1)).findIsCorrectedById(101L);
    }

    @Test
    @DisplayName("5_13")
    void getChoiceList_ProcessMSCorrect_5_13() {
        // Arrange
        AnswerSheet userAnswer = new AnswerSheet();
        userAnswer.setQuestionId(1L);
        List<Choice> choices = new ArrayList<>();
        Choice choice = new Choice();
        choice.setId(101L);
        choice.setIsCorrected(1);
        choices.add(choice);
        userAnswer.setChoices(choices);
        userAnswer.setPoint(10);

        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(1L);
        examQuestionPoint.setPoint(10);

        List<AnswerSheet> userChoices = Arrays.asList(userAnswer);
        List<ExamQuestionPoint> examQuestionPoints = Arrays.asList(examQuestionPoint);

        Question question = new Question();
        question.setId(1L);
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MS);
        question.setQuestionType(questionType);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findIsCorrectedById(101L)).thenReturn(1);

        // Act
        List<ChoiceList> choiceLists = examService.getChoiceList(userChoices, examQuestionPoints);

        // Assert
        assertNotNull(choiceLists);
        assertEquals(1, choiceLists.size());
        ChoiceList choiceList = choiceLists.get(0);
        assertEquals(question, choiceList.getQuestion());
        assertEquals(10, choiceList.getPoint());
        assertTrue(choiceList.getIsSelectedCorrected());

        List<ChoiceCorrect> choiceCorrects = choiceList.getChoices();
        assertEquals(1, choiceCorrects.size());
        assertEquals(1, choiceCorrects.get(0).getIsRealCorrect());

        verify(questionService, times(1)).getQuestionById(1L);
        verify(choiceService, times(1)).findIsCorrectedById(101L);
    }

    @Test
    @DisplayName("5_14")
    void getChoiceList_ProcessMSIncorrect_5_14() {
        // Arrange
        AnswerSheet userAnswer = new AnswerSheet();
        userAnswer.setQuestionId(1L);
        List<Choice> choices = new ArrayList<>();
        Choice choice = new Choice();
        choice.setId(101L);
        choice.setIsCorrected(0);
        choices.add(choice);
        userAnswer.setChoices(choices);
        userAnswer.setPoint(10);

        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(1L);
        examQuestionPoint.setPoint(10);

        List<AnswerSheet> userChoices = Arrays.asList(userAnswer);
        List<ExamQuestionPoint> examQuestionPoints = Arrays.asList(examQuestionPoint);

        Question question = new Question();
        question.setId(1L);
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MS);
        question.setQuestionType(questionType);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findIsCorrectedById(101L)).thenReturn(1);

        // Act
        List<ChoiceList> choiceLists = examService.getChoiceList(userChoices, examQuestionPoints);

        // Assert
        assertNotNull(choiceLists);
        assertEquals(1, choiceLists.size());
        ChoiceList choiceList = choiceLists.get(0);
        assertEquals(question, choiceList.getQuestion());
        assertEquals(10, choiceList.getPoint());
        assertFalse(choiceList.getIsSelectedCorrected());

        List<ChoiceCorrect> choiceCorrects = choiceList.getChoices();
        assertEquals(1, choiceCorrects.size());
        assertEquals(1, choiceCorrects.get(0).getIsRealCorrect());

        verify(questionService, times(1)).getQuestionById(1L);
        verify(choiceService, times(1)).findIsCorrectedById(101L);
    }

    @Test
    @DisplayName("5_15")
    void getChoiceList_InvalidQuestionType_5_15() {
        // Arrange
        AnswerSheet userAnswer = new AnswerSheet();
        userAnswer.setQuestionId(1L);
        List<Choice> choices = new ArrayList<>();
        Choice choice = new Choice();
        choice.setId(101L);
        choice.setIsCorrected(0);
        choices.add(choice);
        userAnswer.setChoices(choices);
        userAnswer.setPoint(10);

        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(1L);
        examQuestionPoint.setPoint(10);

        List<AnswerSheet> userChoices = Arrays.asList(userAnswer);
        List<ExamQuestionPoint> examQuestionPoints = Arrays.asList(examQuestionPoint);

        Question question = new Question();
        question.setId(1L);
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(null);
        question.setQuestionType(questionType);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            examService.getChoiceList(userChoices, examQuestionPoints);
        });

        verify(questionService, times(1)).getQuestionById(1L);
    }
}