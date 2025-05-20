
package com.thanhtam.backend;

import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.service.QuestionServiceImpl;
import com.thanhtam.backend.ultilities.DifficultyLevel;

import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Optional;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ Class kiểm thử tích hợp cho QuestionServiceImpl
 * 🎯 Kiểm tra các hành vi ghi/xóa thực sự vào DB: save(), update(), delete()
 * 💡 Dữ liệu được rollback sau mỗi test nhờ @Transactional
 */
@SpringBootTest
@Transactional
public class QuestionServiceImplTest_DB {

    @Autowired
    private QuestionServiceImpl questionService;

    @Autowired
    private QuestionRepository questionRepository;

    // =====================================================================================
    // ✅ TEST CASE 4.16 - Lưu câu hỏi EASY (GHI VÀO DB)
    // 🎯 Mục đích: kiểm tra lưu thành công vào DB thật với mức độ EASY
    // 🧪 Điều kiện: tạo đối tượng Question với Part và DifficultyLevel, gọi save()
    // ✅ Kết quả mong đợi: ID tự sinh, dữ liệu lưu đúng trong DB
    // =====================================================================================
    @Test
    void testSaveQuestion_shouldPersistInDB() {
        Question q = new Question();
        Part part = new Part(); 
        part.setId(1L);
        q.setPart(part);
       
        q.setQuestionText("Câu hỏi kiểm thử");
        
        q.setDifficultyLevel(DifficultyLevel.EASY);
        q.setDeleted(false);

        questionService.save(q);  // void method
        
        Optional<Question> found = questionRepository.findById(q.getId());
        assertTrue(found.isPresent());
        assertEquals("Câu hỏi kiểm thử", found.get().getQuestionText());
        // Scanner sc = new Scanner(System.in);
        // String s = sc.nextLine();
    }

    // =====================================================================================
    // ✅ TEST CASE 4.20 - Cập nhật câu hỏi thành công
    // 🎯 Mục đích: kiểm tra update thay đổi được lưu lại
    // 🧪 Điều kiện: lưu câu hỏi trước, sau đó sửa text và gọi update()
    // ✅ Kết quả mong đợi: text được cập nhật đúng
    // =====================================================================================
    @Test
    void testUpdateQuestion_shouldUpdateContent() {
        Question q = new Question();
        Part part = new Part();
        part.setId(1L);
        q.setPart(part);
        q.setQuestionText("Ban đầu");
        q.setDifficultyLevel(DifficultyLevel.EASY);
        q.setDeleted(false);

        questionService.save(q);

        q.setQuestionText("Đã cập nhật");
        questionService.update(q);  // void method

        Optional<Question> updated = questionRepository.findById(q.getId());
        assertTrue(updated.isPresent());
        assertEquals("Đã cập nhật", updated.get().getQuestionText());
    }

    // =====================================================================================
    // ✅ TEST CASE 4.22 - Xóa câu hỏi khi tìm thấy
    // 🎯 Mục đích: kiểm tra xoá cứng 1 câu hỏi
    // 🧪 Điều kiện: lưu vào DB → gọi delete(id)
    // ✅ Kết quả mong đợi: không còn record trong DB
    // =====================================================================================
    @Test
    void testDeleteQuestion_shouldDeleteFromDB() {
        Question q = new Question();
        q.setQuestionText("Câu hỏi cần xoá");
        q.setDeleted(false);
        q.setDifficultyLevel(DifficultyLevel.EASY);
        questionService.save(q);
        Long id = q.getId();

        questionService.delete(id);

        Optional<Question> deleted = questionRepository.findById(id);
        assertFalse(deleted.isPresent());
    }
}
