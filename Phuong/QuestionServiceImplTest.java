package com.thanhtam.backend;


import com.thanhtam.backend.dto.*;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.thanhtam.backend.service.QuestionServiceImpl;
import com.thanhtam.backend.ultilities.DifficultyLevel;
import com.thanhtam.backend.ultilities.EQTypeCode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Question question;
    private Part part;
    private QuestionType questionType;

    @BeforeEach
    void setUp() {
        // Initialize entities
        part = new Part();
        part.setId(1L);
        part.setName("Part A");

        // Create a sample QuestionType object using the EQTypeCode enum
        questionType = new QuestionType();
        questionType.setId(1L);
        questionType.setTypeCode(EQTypeCode.MC);
        questionType.setDescription("Multiple Choice");

        // Create a sample Question object and set its properties using setters
        question = new Question();
        question.setId(1L);
        question.setQuestionText("Sample Question");
        question.setPart(part);
        question.setQuestionType(questionType);
        question.setPoint(5);
        question.setChoices(new ArrayList<>());
        question.setDeleted(false);
    }

    /**
     * --- Test case 4.1: Return Question if ID exists ---
     * Test getQuestionById() when the question exists in the repository.
     * Expected Output: Return the question wrapped in an Optional.
     */
    @Test
    void testGetQuestionById_found() {
        // Arrange
        Long id = 1L;
        when(questionRepository.findById(id)).thenReturn(Optional.of(question));

        // Act
        Optional<Question> result = questionService.getQuestionById(id);

        // Assert
        assertTrue(result.isPresent(), "Expected a question to be found");
        assertEquals(question, result.get(), "The returned question should match the expected question");
    }

    /**
     * --- Test case 4.2: Return empty Optional if ID DNE ---
     * Test getQuestionById() when the question does not exist in the repository.
     * Expected Output: Return an empty Optional.
     */
    @Test
    void testGetQuestionById_notFound() {
        // Arrange
        Long id = 999L;
        when(questionRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Question> result = questionService.getQuestionById(id);

        // Assert
        assertFalse(result.isPresent(), "Expected no question to be found");
    }

    /**
     * --- Test case 4.3: Return Questions if Part has questions ---
     * Test getQuestionByPart() when there are questions for the given part.
     * Expected Output: Return a list of questions.
     */
    @Test
    void testGetQuestionByPart_found() {
        // Arrange
        when(questionRepository.findByPart(part)).thenReturn(Collections.singletonList(question));

        // Act
        List<Question> result = questionService.getQuestionByPart(part);

        // Assert
        assertFalse(result.isEmpty(), "Expected a list of questions for the given part");
        assertEquals(1, result.size(), "Expected one question in the list");
        assertEquals(question, result.get(0), "The returned question should match the expected question");
    }

    /**
     * --- Test case 4.4: Return empty List if Part has no Questions ---
     * Test getQuestionByPart() when there are no questions for the given part.
     * Expected Output: Return an empty list.
     */
    @Test
    void testGetQuestionByPart_notFound() {
        // Arrange
        when(questionRepository.findByPart(part)).thenReturn(Collections.emptyList());

        // Act
        List<Question> result = questionService.getQuestionByPart(part);

        // Assert
        assertTrue(result.isEmpty(), "Expected an empty list for the given part");
    }

    /**
     * --- Test case 4.5: Return Questions if type has questions ---
     * Test getQuestionByQuestionType() when there are questions of the given type.
     * Expected Output: Return a list of questions.
     */
    @Test
    void testGetQuestionByQuestionType_found() {
        // Arrange
        when(questionRepository.findByQuestionType(questionType)).thenReturn(Collections.singletonList(question));

        // Act
        List<Question> result = questionService.getQuestionByQuestionType(questionType);

        // Assert
        assertFalse(result.isEmpty(), "Expected a list of questions for the given question type");
        assertEquals(1, result.size(), "Expected one question in the list");
    }

    /**
     * --- Test case 4.6: Return empty List if type has no questions ---
     * Test getQuestionByQuestionType() when there are no questions of the given
     * type.
     * Expected Output: Return an empty list.
     */
    @Test
    void testGetQuestionByQuestionType_notFound() {
        // Arrange
        when(questionRepository.findByQuestionType(questionType)).thenReturn(Collections.emptyList());

        // Act
        List<Question> result = questionService.getQuestionByQuestionType(questionType);

        // Assert
        assertTrue(result.isEmpty(), "Expected an empty list for the given question type");
    }

    /**
     * --- Test case 4.7: Return AnswerSheet list with the Question list ---
     * Test convertFromQuestionList() when a valid list of questions is passed.
     * Expected Output: Return a list of AnswerSheet objects corresponding to the
     * questions.
     */
    @Test
    void testConvertFromQuestionList_validQuestions() {
        // Arrange
        List<Question> questionList = Collections.singletonList(question);

        // Act
        List<AnswerSheet> result = questionService.convertFromQuestionList(questionList);

        // Assert
        assertFalse(result.isEmpty(), "Expected non-empty list of AnswerSheet");
        assertEquals(1, result.size(), "Expected one AnswerSheet in the list");
    }

    /**
     * --- Test case 4.8: QuestionList empty ---
     * Test convertFromQuestionList() when an empty list of questions is passed.
     * Expected Output: Return an empty list.
     */
    @Test
    void testConvertFromQuestionList_emptyQuestions() {
        // Arrange
        List<Question> questionList = new ArrayList<>();

        // Act
        List<AnswerSheet> result = questionService.convertFromQuestionList(questionList);

        // Assert
        assertTrue(result.isEmpty(), "Expected an empty list for empty question list");
    }

    /**
     * --- Test case 4.9: Return all Question ---
     * Test getQuestionList() when there are questions in the repository.
     * Expected Output: Return a list of all questions.
     */
    @Test
    void testGetQuestionList_found() {
        // Arrange
        when(questionRepository.findAll()).thenReturn(Collections.singletonList(question));

        // Act
        List<Question> result = questionService.getQuestionList();

        // Assert
        assertFalse(result.isEmpty(), "Expected a list of questions to be returned");
        assertEquals(1, result.size(), "Expected one question in the list");
    }

    /**
     * --- Test case 4.10: question table empty ---
     * Test getQuestionList() when the repository has no questions.
     * Expected Output: Return an empty list.
     */
    @Test
    void testGetQuestionList_empty() {
        // Arrange
        when(questionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Question> result = questionService.getQuestionList();

        // Assert
        assertTrue(result.isEmpty(), "Expected an empty list for empty table");
    }

    /**
     * --- Test case 4.11: Page has content ---
     * Test findQuestionsByPart() when there are questions for the given part.
     * Expected Output: Return a page of questions.
     */
    @Test
    void testFindQuestionsByPart_found() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<Question> page = new PageImpl<>(Collections.singletonList(question));
        when(questionRepository.findQuestionsByPart(pageable, part)).thenReturn(page);

        // Act
        Page<Question> result = questionService.findQuestionsByPart(pageable, part);

        // Assert
        assertFalse(result.isEmpty(), "Expected a page with questions for the given part");
    }

    /**
     * --- Test case 4.12: No record found ---
     * Test findQuestionsByPart() when there are no questions for the given part.
     * Expected Output: Return an empty page.
     */
    @Test
    void testFindQuestionsByPart_notFound() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<Question> page = new PageImpl<>(Collections.emptyList());
        when(questionRepository.findQuestionsByPart(pageable, part)).thenReturn(page);

        // Act
        Page<Question> result = questionService.findQuestionsByPart(pageable, part);

        // Assert
        assertTrue(result.isEmpty(), "Expected an empty page for the given part");
    }

    /**
     * --- Test case 4.13: Save Question EASY ---
     * Test save() when saving a question with a specific difficulty level.
     * Expected Output: The question should be saved with the correct point value.
     */
    @Test
    void testSave_validQuestion_easy() {
        // Arrange
        question.setDifficultyLevel(DifficultyLevel.EASY);

        // Act
        questionService.save(question);

        // Assert
        verify(questionRepository, times(1)).save(question);
        assertEquals(5, question.getPoint(), "The point should be 5 for EASY difficulty");
    }

    /**
     * --- Test case 4.14: Save Question MEDIUM ---
     * Test save() when saving a question with MEDIUM difficulty.
     * Expected Output: The question should be saved with the correct point value.
     */
    @Test
    void testSave_validQuestion_medium() {
        // Arrange
        question.setDifficultyLevel(DifficultyLevel.MEDIUM);

        // Act
        questionService.save(question);

        // Assert
        verify(questionRepository, times(1)).save(question);
        assertEquals(10, question.getPoint(), "The point should be 10 for MEDIUM difficulty");
    }

    /**
     * --- Test case 4.15: Save Question HARD ---
     * Test save() when saving a question with HARD difficulty.
     * Expected Output: The question should be saved with the correct point value.
     */
    @Test
    void testSave_validQuestion_hard() {
        // Arrange
        question.setDifficultyLevel(DifficultyLevel.HARD);

        // Act
        questionService.save(question);

        // Assert
        verify(questionRepository, times(1)).save(question);
        assertEquals(15, question.getPoint(), "The point should be 15 for HARD difficulty");
    }

    // --- Test case 4.16: Save Question DEFAULT (no difficulty) ---
    // Given that DifficultyLevel requires a valid value, we will test the behavior
    // of saving a question
    // with an invalid or null difficulty level (if supported by the system).
    // This will ensure that the system handles invalid difficulty levels
    // gracefully.
    @Test
    void testSaveQuestionWithInvalidDifficulty() {
        // Create a new Question without setting a difficulty level (assuming null is
        // not allowed)
        Question question = new Question();
        question.setId(1L);
        question.setQuestionText("Sample Question with no difficulty");
        question.setPart(part);
        question.setQuestionType(questionType);
        question.setPoint(5);
        question.setChoices(new ArrayList<>());
        question.setDeleted(false);

        // Set an invalid difficulty level (if such a case is possible)
        question.setDifficultyLevel(null);

        // Use assertThrows to expect an exception when saving the question with invalid
        // difficulty level
        assertThrows(IllegalArgumentException.class, () -> {
            questionService.save(question); // Wrap the method call inside the lambda expression
        });
    }

    /**
     * --- Test case 4.17: Update Question ---
     * Test update() when updating an existing question.
     * Expected Output: The updated question should match the new values.
     */
    @Test
    void testUpdate_validQuestion() {
        // Arrange
        question.setId(1L);
        question.setQuestionText("Updated Question"); // Use the correct setter for the question text
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // Act
        questionService.update(question);

        // Assert
        verify(questionRepository, times(1)).save(question); // Verify that save was called exactly once
        assertEquals("Updated Question", question.getQuestionText(), "The question text should be updated"); // Use the
                                                                                                             // correct
                                                                                                             // getter
                                                                                                             // for the
                                                                                                             // question
                                                                                                             // text
    }

    /**
     * --- Test case 4.18: Delete Question ---
     * Test delete() when deleting an existing question.
     * Expected Output: The question should be removed from the repository.
     */
    @Test
    void testDelete_validQuestion() {
        // Arrange
        question.setId(1L);

        // Act
        questionService.delete(1L);

        // Assert
        verify(questionRepository, times(1)).deleteById(1L);
    }

    /**
     * --- Test case 4.19: Return Question if ID exists and is not deleted ---
     * Test getQuestionById() when the question exists in the repository and is not
     * marked as deleted.
     * Expected Output: Return the question wrapped in an Optional.
     */
    @Test
    void testGetQuestionById_notDeleted() {
        // Arrange
        Long id = 1L;
        question.setDeleted(false); // Ensure the question is not deleted
        when(questionRepository.findById(id)).thenReturn(Optional.of(question));

        // Act
        Optional<Question> result = questionService.getQuestionById(id);

        // Assert
        assertTrue(result.isPresent(), "Expected a question to be found");
        assertFalse(result.get().isDeleted(), "Expected the question to be not deleted");
    }

    /**
     * --- Test case 4.20: Return empty Optional if ID exists but deleted ---
     * Test getQuestionById() when the question exists but is marked as deleted.
     * Expected Output: Return an empty Optional.
     */
    @Test
    void testGetQuestionById_deleted() {
        // Arrange
        Long id = 1L;
        question.setDeleted(true); // Mark the question as deleted
        when(questionRepository.findById(id)).thenReturn(Optional.of(question));

        // Act
        Optional<Question> result = questionService.getQuestionById(id);

        // Assert
        assertFalse(result.isPresent(), "Expected no question to be found as it is deleted");
    }

    /**
     * --- Test case 4.21: Return Questions with multiple parts ---
     * Test getQuestionByPart() when multiple questions exist for the given part.
     * Expected Output: Return a list of questions.
     */
    @Test
    void testGetQuestionByPart_multipleQuestions() {
        // Arrange
        List<Question> questionList = Arrays.asList(question, new Question());
        when(questionRepository.findByPart(part)).thenReturn(questionList);

        // Act
        List<Question> result = questionService.getQuestionByPart(part);

        // Assert
        assertFalse(result.isEmpty(), "Expected a list of questions for the given part");
        assertEquals(2, result.size(), "Expected two questions in the list");
    }

    /**
     * --- Test case 4.22: Return empty List if Part has deleted Questions ---
     * Test getQuestionByPart() when there are questions in the part, but they are
     * all deleted.
     * Expected Output: Return an empty list.
     */
    @Test
    void testGetQuestionByPart_allDeleted() {
        // Arrange
        Question deletedQuestion = new Question();
        deletedQuestion.setDeleted(true);
        when(questionRepository.findByPart(part)).thenReturn(Collections.singletonList(deletedQuestion));

        // Act
        List<Question> result = questionService.getQuestionByPart(part);

        // Assert
        assertTrue(result.isEmpty(), "Expected an empty list as all questions are deleted");
    }

    /**
     * --- Test case 4.23: Has question, Deleted False ---
     * Test retrieval of questions with valid partId and username, where the
     * question is not deleted.
     * Expected Output: Return a page of questions with content (questions should be
     * found).
     */
    @Test
    void testFindQuestionsByPartAndUsername_found() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Long partId = 1L; // Valid part ID
        String username = "validUser"; // Valid username
        Page<Question> page = new PageImpl<>(Collections.singletonList(question));
        when(questionRepository.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(pageable, partId, username))
                .thenReturn(page);

        // Act
        Page<Question> result = questionService.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(pageable,
                partId, username);

        // Assert
        assertTrue(result.hasContent(), "Expected a page with questions to be returned");
        assertEquals(1, result.getTotalElements(), "Expected one question in the page");
    }

    /**
     * --- Test case 4.24: No record for User and Part found ---
     * Test retrieval of questions when there are no questions for the given user or
     * part.
     * Expected Output: Return an empty page (no questions found).
     */
    @Test
    void testFindQuestionsByPartAndUsername_notFound() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Long partId = 1L; // Valid part ID
        String username = "validUser"; // Valid username
        Page<Question> page = new PageImpl<>(Collections.emptyList());
        when(questionRepository.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(pageable, partId, username))
                .thenReturn(page);

        // Act
        Page<Question> result = questionService.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(pageable,
                partId, username);

        // Assert
        assertFalse(result.hasContent(), "Expected an empty page of questions to be returned");
    }

    /**
     * --- Test case 4.25: Has question and record found ---
     * Test retrieval of all questions using pagination when questions are present
     * in the database.
     * Expected Output: Return a page of questions with content.
     */
    @Test
    void testFindAllQuestions_found() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<Question> page = new PageImpl<>(Collections.singletonList(question));
        when(questionRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Question> result = questionService.findAllQuestions(pageable);

        // Assert
        assertTrue(result.hasContent(), "Expected a page with questions to be returned");
        assertEquals(1, result.getTotalElements(), "Expected one question in the page");
    }

    /**
     * --- Test case 4.26: No record found ---
     * Test retrieval of all questions when there are no questions in the database.
     * Expected Output: Return an empty page (no questions found).
     */
    @Test
    void testFindAllQuestions_notFound() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<Question> page = new PageImpl<>(Collections.emptyList());
        when(questionRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Question> result = questionService.findAllQuestions(pageable);

        // Assert
        assertFalse(result.hasContent(), "Expected an empty page of questions to be returned");
    }

    /**
     * --- Test case 4.27: Return Question Text by ID ---
     * Test findQuestionTextById() when the question exists in the repository.
     * Expected Output: Return the question text if found by ID.
     */
    @Test
    void testFindQuestionTextById_found() {
        // Arrange
        Long questionId = 1L; // Valid question ID
        String expectedText = "What is the capital of France?"; // Example question text
        when(questionRepository.findQuestionTextById(questionId)).thenReturn(expectedText);

        // Act
        String result = questionService.findQuestionTextById(questionId);

        // Assert
        assertNotNull(result, "Expected the question text to be found");
        assertEquals(expectedText, result, "The returned question text should match the expected text");
    }

    /**
     * --- Test case 4.28: Update Question with deleted status ---
     * Test update() when the question is marked as deleted.
     * Expected Output: Throw an IllegalStateException.
     */
    @Test
    void testUpdate_deletedQuestion() {
        // Arrange
        question.setDeleted(true); // Mark as deleted
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> questionService.update(question),
                "Expected IllegalStateException to be thrown as the question is deleted");
    }

    /**
     * --- Test case 4.29: Check Question Delete not called if deleted ---
     * Test delete() when a question is already marked as deleted.
     * Expected Output: Ensure that deleteById is not called.
     */
    @Test
    void testDelete_alreadyDeletedQuestion() {
        // Arrange
        question.setDeleted(true); // Mark as deleted

        // Act
        questionService.delete(1L);

        // Assert
        verify(questionRepository, times(0)).deleteById(1L); // Ensure deleteById is not called
    }

    /**
     * --- Test case 4.30: Return paged result if part has more than 10 questions
     * ---
     * Test getQuestionList() when the questions exceed the page size.
     * Expected Output: Ensure that the results are paged correctly.
     */
    @Test
    void testFindQuestionsByPart_paged() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        List<Question> questionList = Arrays.asList(question, new Question(), new Question());
        Page<Question> page = new PageImpl<>(questionList);
        when(questionRepository.findQuestionsByPart(pageable, part)).thenReturn(page);

        // Act
        Page<Question> result = questionService.findQuestionsByPart(pageable, part);

        // Assert
        assertFalse(result.isEmpty(), "Expected a page with questions to be returned");
        assertEquals(3, result.getTotalElements(), "Expected three questions in the page");
    }

    /**
     * --- Test case 4.31: Save Question with point validation ---
     * Test save() when saving a question with an invalid point value.
     * Expected Output: Throw an IllegalArgumentException for invalid points.
     */
    @Test
    void testSave_invalidPointValue() {
        // Arrange
        question.setPoint(-5); // Set invalid negative point value

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> questionService.save(question),
                "Expected IllegalArgumentException to be thrown for invalid point value");
    }

    /**
     * --- Test case 4.32: Update Question point validation ---
     * Test update() when updating a question with invalid point value.
     * Expected Output: Throw an IllegalArgumentException for invalid points.
     */
    @Test
    void testUpdate_invalidPointValue() {
        // Arrange
        question.setPoint(-5); // Set invalid negative point value
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> questionService.update(question),
                "Expected IllegalArgumentException to be thrown for invalid point value");
    }

}
