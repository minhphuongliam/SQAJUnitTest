package com.thanhtam.backend;

// Import necessary classes and services for unit testing
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

// Enable Mockito support for dependency injection
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

    // Inject mocks into the service being tested
    @InjectMocks
    private ExamServiceImpl examService;

    private Exam exam;

    // Common setup for each test
    @BeforeEach
    void setUp() {
        // Initializes a sample Exam object before each test
        exam = new Exam();
        exam.setId(1L);
        exam.setTitle("Test Exam");
    }

    // Test saving a new exam
    @Test
    @DisplayName("5_1")
    void saveNewExam_L5_1() {
        // Test saving a new Exam object using the service layer
        Exam newExam = new Exam();
        newExam.setTitle("New Exam");
        when(examRepository.save(newExam)).thenReturn(exam);

        Exam savedExam = examService.saveExam(newExam);

        assertEquals(exam, savedExam);
        verify(examRepository, times(1)).save(newExam);
    }

    // Test updating an existing exam
    @Test
    @DisplayName("L5_2")
    void updateExistingExam_5_2() {
        // Test updating an existing exam entity
        exam.setTitle("Updated Exam");
        when(examRepository.save(exam)).thenReturn(exam);

        Exam updatedExam = examService.saveExam(exam);

        assertEquals(exam, updatedExam);
        verify(examRepository, times(1)).save(exam);
    }

    // Test retrieving all exams with pagination
    @Test
    @DisplayName("5_3")
    void findAllExamsWithPagination_5_3() {
        // Test retrieving paginated list of exams
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        List<Exam> exams = Arrays.asList(exam);
        Page<Exam> examPage = new PageImpl<>(exams, pageable, exams.size());

        when(examRepository.findAll(pageable)).thenReturn(examPage);

        Page<Exam> result = examService.findAll(pageable);

        assertEquals(examPage, result);
        verify(examRepository, times(1)).findAll(pageable);
    }

    // Test canceling an exam by ID
    @Test
    @DisplayName("5_4")
    void cancelExam_5_4() {
        // Verifies that the cancelExam method properly calls repository method
        Long examId = 1L;
        examService.cancelExam(examId);
        verify(examRepository, times(1)).cancelExam(examId);
    }

    // Test retrieving all exams without pagination
    @Test
    @DisplayName("5_5")
    void getAllExams_5_5() {
        // Test retrieving all exams from the repository
        List<Exam> exams = Arrays.asList(exam);
        when(examRepository.findAll()).thenReturn(exams);

        List<Exam> result = examService.getAll();

        assertEquals(exams, result);
        verify(examRepository, times(1)).findAll();
    }

    // Test retrieving an exam by ID (found)
    @Test
    @DisplayName("5_6")
    void getExamById_5_6() {
        // Test retrieving an exam by its ID when the exam exists
        Long examId = 10L;
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

        Optional<Exam> result = examService.getExamById(examId);

        assertTrue(result.isPresent());
        assertEquals(exam, result.get());
        verify(examRepository, times(1)).findById(examId);
    }

    // Test retrieving an exam by ID (not found)
    @Test
    @DisplayName("5_7")
    void getExamByIdNotFound_5_7() {
        // Test retrieving an exam by ID when it does not exist
        Long examId = 13L;
        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        Optional<Exam> result = examService.getExamById(examId);

        assertFalse(result.isPresent());
        verify(examRepository, times(1)).findById(examId);
    }

    // Test retrieving exams by the creator's username
    @Test
    @DisplayName("5_8")
    void findAllByCreatedByUsername_5_8() {
        // Test retrieving paginated exams created by a specific user
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

        // Test for True/False question type with correct selected answer
        // Verifies that the correct choice is marked and point is assigned

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
        
        // Test for True/False question type with incorrect answer selected
        // Verifies that the answer is marked incorrect
        
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
        // Test for Multiple Choice question with correct answer selected
        // Should mark the question as correctly answered
        
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
        // Test for Multiple Choice with incorrect selection
        // Should result in an incorrect answer flag

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
        // Test for Multiple Select (MS) where the student correctly selects all correct answers
        // Should be marked as correct
        
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
        // Test for Multiple Select (MS) with partial or incorrect selections
        // Should be marked as incorrect
        
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
        // Test for invalid or null question type
        // Should throw NullPointerException or handle appropriately
        
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