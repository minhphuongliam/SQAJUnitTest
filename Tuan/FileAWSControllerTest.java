package com.thanhtam.backend;

import com.thanhtam.backend.controller.FileAWSController;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.service.S3Services;
import com.thanhtam.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileAWSControllerTest {

    @Mock
    private S3Services s3Services;

    @Mock
    private UserService userService;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private FileAWSController controller;

    private final String BUCKET = "test-bucket";
    private final String ENDPOINT = "http://localhost";
    private final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        // set @Value fields
        ReflectionTestUtils.setField(controller, "bucketName", BUCKET);
        ReflectionTestUtils.setField(controller, "endpointUrl", ENDPOINT);
    }

    @Test
    @DisplayName("uploadMultipartFile should handle avatar upload and update profile")
    void uploadMultipartFile_Avatar_ShouldReturnUrlAndUpdateProfile() {
        when(mockFile.getOriginalFilename()).thenReturn("pic.jpg");
        when(userService.getUserName()).thenReturn(USERNAME);
        Profile profile = new Profile();
        User user = new User(); user.setProfile(profile);
        when(userService.getUserByUsername(USERNAME)).thenReturn(Optional.of(user));

        String result = controller.uploadMultipartFile(mockFile, "avatar");

        String expectedKey = USERNAME + "-avatar.jpg";
        verify(s3Services).uploadS3File(eq(expectedKey), eq(mockFile));
        assertEquals(ENDPOINT + "/" + BUCKET + "/" + expectedKey, result);
        assertEquals(result, user.getProfile().getImage());
    }

    @Test
    @DisplayName("uploadMultipartFile should handle course upload and generate dated key")
    void uploadMultipartFile_Course_ShouldGenerateDatedKey() {
        when(mockFile.getOriginalFilename()).thenReturn("course.png");

        String result = controller.uploadMultipartFile(mockFile, "course");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(s3Services).uploadS3File(keyCaptor.capture(), eq(mockFile));
        String capturedKey = keyCaptor.getValue();
        assertTrue(capturedKey.endsWith("course.png"));
        // key starts with date string
        assertTrue(capturedKey.length() > "course.png".length());
        assertEquals(ENDPOINT + "/" + BUCKET + "/" + capturedKey, result);
    }

    @Test
    @DisplayName("uploadMultipartFile should handle default upload when fileAs is other")
    void uploadMultipartFile_Default_ShouldUseOriginalFilename() {
        when(mockFile.getOriginalFilename()).thenReturn("doc.txt");

        String result = controller.uploadMultipartFile(mockFile, "other");

        verify(s3Services).uploadS3File(eq("doc.txt"), eq(mockFile));
        assertEquals(ENDPOINT + "/" + BUCKET + "/doc.txt", result);
    }

    @Test
    @DisplayName("uploadCourseImg should upload and return url with dated key")
    void uploadCourseImg_ShouldUploadAndReturnUrl() {
        when(mockFile.getOriginalFilename()).thenReturn("img.jpg");

        String result = controller.uploadCourseImg(mockFile);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(s3Services).uploadS3File(keyCaptor.capture(), eq(mockFile));
        String key = keyCaptor.getValue();
        assertTrue(key.endsWith("img.jpg"));
        assertEquals(ENDPOINT + "/" + BUCKET + "/" + key, result);
    }

    @Test
    @DisplayName("uploadMultipartFile avatar endpoint should upload, update and return url")
    void uploadMultipartFile_AvatarEndpoint_ShouldUpdateUserAndReturnUrl() throws Exception {
        when(mockFile.getOriginalFilename()).thenReturn("avatar.png");
        when(userService.getUserName()).thenReturn(USERNAME);
        Profile profile = new Profile(); profile.setImage("");
        User user = new User(); user.setProfile(profile);
        when(userService.getUserByUsername(USERNAME)).thenReturn(Optional.of(user));

        String result = controller.uploadMultipartFile(mockFile);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(s3Services).uploadS3File(keyCaptor.capture(), eq(mockFile));
        String key = keyCaptor.getValue();
        assertTrue(key.contains(USERNAME + "_avatar.png"));
        verify(userService).updateUser(user);
        assertEquals(ENDPOINT + "/" + BUCKET + "/" + key, result);
        assertEquals(result, user.getProfile().getImage());
    }

    @Test
    @DisplayName("uploadMultipartFile avatar endpoint should throw if upload fails")
    void uploadMultipartFile_AvatarEndpoint_ShouldPropagateException() throws Exception {
        when(mockFile.getOriginalFilename()).thenReturn("fail.png");
        when(userService.getUserName()).thenReturn(USERNAME);
        User user = new User(); user.setProfile(new Profile());
        when(userService.getUserByUsername(USERNAME)).thenReturn(Optional.of(user));
        doThrow(new IOException("upload error")).when(s3Services).uploadS3File(any(), any());

        Exception ex = assertThrows(Exception.class, () -> controller.uploadMultipartFile(mockFile));
        assertTrue(ex.getMessage().contains("upload error"));
    }

    @Test
    @DisplayName("uploadMultipartFile avatar endpoint should throw when user not found")
    void uploadMultipartFile_AvatarEndpoint_UserNotFound_ShouldThrow() {
        when(userService.getUserName()).thenReturn(USERNAME);
        when(userService.getUserByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> controller.uploadMultipartFile(mockFile));
    }

    @Test
    @DisplayName("downloadFile should return file bytes with correct headers and type")
    void downloadFile_ShouldReturnResponseEntity() {
        String keyName = "picture.jpg";
        byte[] data = "abc".getBytes();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try { stream.write(data); } catch (IOException ignored) {}
        when(s3Services.downloadS3File(keyName)).thenReturn(stream);

        ResponseEntity<byte[]> response = controller.downloadFile(keyName);

        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains(keyName));
        assertArrayEquals(data, response.getBody());
    }

    @Test
    @DisplayName("listAllFiles should delegate to S3Services")
    void listAllFiles_ShouldReturnList() {
        List<String> files = Arrays.asList("a.txt", "b.png");
        when(s3Services.listS3Files()).thenReturn(files);

        List<String> result = controller.listAllFiles();

        assertEquals(files, result);
    }

    @Test
    @DisplayName("deleteFile should delete and return confirmation message")
    void deleteFile_ShouldDeleteAndReturnMessage() {
        String key = "del.txt";

        ResponseEntity<String> response = controller.deleteFile(key);

        verify(s3Services).deleteFile(key);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("[del.txt] deleted successfully.", response.getBody());
    }
}
