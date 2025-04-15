package com.thanhtam.backend;

import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.service.ExcelServiceImpl;
import com.thanhtam.backend.service.FilesStorageService;
import com.thanhtam.backend.service.IntakeService;
import com.thanhtam.backend.service.RoleService;
import com.thanhtam.backend.ultilities.ERole;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExcelServiceImplTest {

    @Mock
    private FilesStorageService filesStorageService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private IntakeService intakeService;

    @InjectMocks // Removed @Spy to avoid problems with private methods
    private ExcelServiceImpl excelService;

    private String excelFilePath;

    @BeforeEach
    void setUp() {
        excelFilePath = "test.xlsx";
    }

    @Test
    @DisplayName("7_1")
    void readUserFromExcelFile_xlsx_ValidRoles_7_1() throws IOException {
                // Mock Excel file reading
                String mockExcelContent = "Username,Email,FirstName,LastName,IntakeCode,Role\n" +
                "testuser1,test1@example.com,John,Doe,ITC1,ADMIN\n" +
                "testuser2,test2@example.com,Jane,Smith,ITC2,LECTURER\n" +
                "testuser3,test3@example.com,Peter,Jones,ITC3,STUDENT";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockExcelContent.getBytes());

        // Load the workbook using the input stream
        Workbook mockWorkbook = new XSSFWorkbook(inputStream);
        Sheet mockSheet = mockWorkbook.getSheetAt(0);

        if (mockSheet == null) {
            mockSheet = mockWorkbook.createSheet();
            for (int i = 0; i < mockExcelContent.split("\n").length; i++) {
                Row row = mockSheet.createRow(i);
                String[] cells = mockExcelContent.split("\n")[i].split(",");
                for (int j = 0; j < cells.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(cells[j]);
                }
            }
        }

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(intakeService.findByCode(anyString())).thenReturn(new Intake());

        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        Role lecturerRole = new Role();
        lecturerRole.setName(ERole.ROLE_LECTURER);
        when(roleService.findByName(ERole.ROLE_LECTURER)).thenReturn(Optional.of(lecturerRole));

        Role studentRole = new Role();
        studentRole.setName(ERole.ROLE_STUDENT);
        when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));

        List<User> userList = excelService.readUserFromExcelFile(excelFilePath);

        assertEquals(3, userList.size());
        assertEquals(ERole.ROLE_ADMIN, userList.get(0).getRoles().stream().findFirst().get().getName());
        assertEquals(ERole.ROLE_LECTURER, userList.get(1).getRoles().stream().findFirst().get().getName());
        assertEquals(ERole.ROLE_STUDENT, userList.get(2).getRoles().stream().findFirst().get().getName());
    }

    @Test
    @DisplayName("7_2")
    void readUserFromExcelFile_xls_7_2() throws IOException {
        //SETUP
        String mockExcelContent = "Username,Email,FirstName,LastName,IntakeCode,Role\n" +
                "testuser1,test1@example.com,John,Doe,ITC1,ADMIN\n" +
                "testuser2,test2@example.com,Jane,Smith,ITC2,LECTURER\n" +
                "testuser3,test3@example.com,Peter,Jones,ITC3,STUDENT";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockExcelContent.getBytes());

        // Mock WorkBook, Sheet and Rows with content
        Workbook mockWorkbook = new HSSFWorkbook(); // Use HSSFWorkbook for .xls
        Sheet mockSheet = mockWorkbook.createSheet();
        if(mockSheet == null) {
            mockSheet = mockWorkbook.createSheet();
            for (int i = 0; i < mockExcelContent.split("\n").length; i++) {
                Row row = mockSheet.createRow(i);
                String[] cells = mockExcelContent.split("\n")[i].split(",");
                for (int j = 0; j < cells.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(cells[j]);
                }
            }
        }

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(intakeService.findByCode(anyString())).thenReturn(new Intake());

        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        Role lecturerRole = new Role();
        lecturerRole.setName(ERole.ROLE_LECTURER);
        when(roleService.findByName(ERole.ROLE_LECTURER)).thenReturn(Optional.of(lecturerRole));

        Role studentRole = new Role();
        studentRole.setName(ERole.ROLE_STUDENT);
        when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));
        //SETUP
        String filePath = "test.xls";

        //EXECUTE
        List<User> userList = excelService.readUserFromExcelFile(filePath);

        //ASSERT
        assertEquals(3, userList.size());
        assertEquals(ERole.ROLE_ADMIN, userList.get(0).getRoles().stream().findFirst().get().getName());
        assertEquals(ERole.ROLE_LECTURER, userList.get(1).getRoles().stream().findFirst().get().getName());
        assertEquals(ERole.ROLE_STUDENT, userList.get(2).getRoles().stream().findFirst().get().getName());
    }

    @Test
    @DisplayName("7_3")
    void readUserFromExcelFile_InvalidFileFormat_7_3() {
        String filePath = "test.txt";

        assertThrows(IllegalArgumentException.class, () -> {
            excelService.readUserFromExcelFile(filePath);
        });
    }

    @Test
    @DisplayName("7_4")
    void readUserFromExcelFile_InvalidRole_Default_7_4() throws IOException {
        // Mock Excel file reading
        String mockExcelContent = "Username,Email,FirstName,LastName,IntakeCode,Role\n" +
                "testuser1,test1@example.com,John,Doe,ITC1,UNKNOWN";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockExcelContent.getBytes());

        // Mock WorkBook, Sheet and Rows with content
        Workbook mockWorkbook = new XSSFWorkbook(); // Use XSSFWorkbook for .xls
        Sheet mockSheet = mockWorkbook.createSheet();
        if(mockSheet == null) {
            mockSheet = mockWorkbook.createSheet();
            for (int i = 0; i < mockExcelContent.split("\n").length; i++) {
                Row row = mockSheet.createRow(i);
                String[] cells = mockExcelContent.split("\n")[i].split(",");
                for (int j = 0; j < cells.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(cells[j]);
                }
            }
        }

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(intakeService.findByCode(anyString())).thenReturn(new Intake());

        Role studentRole = new Role();
        studentRole.setName(ERole.ROLE_STUDENT);
        when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));

        //EXECUTE
        List<User> userList = excelService.readUserFromExcelFile(excelFilePath);

        //ASSERT
        assertEquals(1, userList.size());
        assertEquals(ERole.ROLE_STUDENT, userList.get(0).getRoles().stream().findFirst().get().getName());
    }

    @Test
    @DisplayName("7_5")
    void readUserFromExcelFile_ProfileAssignment_7_5() throws IOException {
        // Mock Excel file reading
        String mockExcelContent = "Username,Email,FirstName,LastName,IntakeCode,Role\n" +
                "testuser1,test1@example.com,John,Doe,ITC1,STUDENT";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockExcelContent.getBytes());

        // Mock WorkBook, Sheet and Rows with content
        Workbook mockWorkbook = new XSSFWorkbook(); // Use XSSFWorkbook for .xls
        Sheet mockSheet = mockWorkbook.createSheet();
        if(mockSheet == null) {
            mockSheet = mockWorkbook.createSheet();
            for (int i = 0; i < mockExcelContent.split("\n").length; i++) {
                Row row = mockSheet.createRow(i);
                String[] cells = mockExcelContent.split("\n")[i].split(",");
                for (int j = 0; j < cells.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(cells[j]);
                }
            }
        }

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(intakeService.findByCode(anyString())).thenReturn(new Intake());

        Role studentRole = new Role();
        studentRole.setName(ERole.ROLE_STUDENT);
        when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));

        //EXECUTE
        List<User> userList = excelService.readUserFromExcelFile(excelFilePath);

        //ASSERT
        assertEquals(1, userList.size());
        assertEquals("John", userList.get(0).getProfile().getFirstName());
        assertEquals("Doe", userList.get(0).getProfile().getLastName());
    }

    @Test
    @DisplayName("7_6")
    void writeUserToExcelFile_7_6() throws IOException {
        List<UserExport> userExports = new ArrayList<>();
        userExports.add(new UserExport("testuser", "test@example.com", "John", "Doe"));

        excelService.writeUserToExcelFile((ArrayList<UserExport>) userExports);

        // Assertions: Check if the file "users.xlsx" is created
        File file = new File("users.xlsx");
        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        file.delete();
    }

    @Test
    @DisplayName("7_7")
    void writeUserToExcelFile_EmptyList_7_7() throws IOException {
        List<UserExport> userExports = new ArrayList<>();

        excelService.writeUserToExcelFile((ArrayList<UserExport>) userExports);

        File file = new File("users.xlsx");
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
        file.delete();
    }

    @Test
    @DisplayName("7_8")
    void writeUserToExcelFile_IOException_7_8() throws IOException {
        List<UserExport> userExports = new ArrayList<>();
        userExports.add(new UserExport("testuser", "test@example.com", "John", "Doe"));
    
        doThrow(new IOException("Simulated IO Exception")).when(filesStorageService).initExcelFolder();
    
        assertThrows(IOException.class, () -> {
            excelService.writeUserToExcelFile((ArrayList<UserExport>) userExports);
        });
    }

    @Test
    @DisplayName("7_9")
    void insertUserToDB_NewUser_7_9() {
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("new@example.com");

        when(userRepository.existsByEmailOrUsername(user.getEmail(), user.getUsername())).thenReturn(false);
        excelService.InsertUserToDB(Arrays.asList(user));

        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("7_10")
    void insertUserToDB_ExistingUser_7_10() {
        User user = new User();
        user.setUsername("existinguser");
        user.setEmail("existing@example.com");

        when(userRepository.existsByEmailOrUsername(user.getEmail(), user.getUsername())).thenReturn(true);

        excelService.InsertUserToDB(Arrays.asList(user));

        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("7_11")
    void insertUserToDB_ExceptionCaught_7_11() {
        User user = new User();
        user.setUsername("existinguser");
        user.setEmail("existing@example.com");

        when(userRepository.existsByEmailOrUsername(user.getEmail(), user.getUsername())).thenReturn(true);

        excelService.InsertUserToDB(Arrays.asList(user));
    }
}