package com.thanhtam.backend;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import com.thanhtam.backend.service.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3ServicesImplTest {

    @Mock
    private AmazonS3 s3client;

    @InjectMocks
    private S3ServicesImpl s3Services;

    @Mock
    private MultipartFile mockFile;

    private final String TEST_BUCKET = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Services, "bucketName", TEST_BUCKET);
    }

    // downloadS3File tests
    @Test
    @DisplayName("2.1 - downloadS3File happy path with valid file")
    void downloadS3File_WhenValidFile_ShouldReturnByteArrayWithData() throws IOException {
        // Arrange
        String keyName = "valid-file.txt";
        String fileContent = "Test file content";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
        
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(inputStream, null);
        
        when(s3client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);
        
        // Act
        ByteArrayOutputStream result = s3Services.downloadS3File(keyName);
        
        // Assert
        assertNotNull(result);
        assertEquals(fileContent, result.toString());
        verify(s3client).getObject(any(GetObjectRequest.class));
    }
    
    @Test
    @DisplayName("2.2 - downloadS3File with IOException during stream read")
    void downloadS3File_WhenIOExceptionOccurs_ShouldReturnNull() throws IOException {
        // Arrange
        String keyName = "io-error.txt";
        
        S3Object s3Object = mock(S3Object.class);
        InputStream mockStream = mock(InputStream.class);
        when(mockStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("Read error"));
        
        when(s3client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(mockStream, null));
        
        // Act
        ByteArrayOutputStream result = s3Services.downloadS3File(keyName);
        
        // Assert
        assertNull(result);
        verify(s3client).getObject(any(GetObjectRequest.class));
    }
    
    @Test
    @DisplayName("2.3 - downloadS3File with AmazonServiceException")
    void downloadS3File_WhenAmazonServiceExceptionOccurs_ShouldThrowException() {
        // Arrange
        String keyName = "service-error.txt";
        AmazonServiceException exception = new AmazonServiceException("Service error");
        
        when(s3client.getObject(any(GetObjectRequest.class))).thenThrow(exception);
        
        // Act & Assert
        AmazonServiceException thrown = assertThrows(AmazonServiceException.class, 
                () -> s3Services.downloadS3File(keyName));
        
        assertEquals("Service error", thrown.getMessage());
        verify(s3client).getObject(any(GetObjectRequest.class));
    }
    
    @Test
    @DisplayName("2.4 - downloadS3File with AmazonClientException")
    void downloadS3File_WhenAmazonClientExceptionOccurs_ShouldThrowException() {
        // Arrange
        String keyName = "client-error.txt";
        AmazonClientException exception = new AmazonClientException("Client error");
        
        when(s3client.getObject(any(GetObjectRequest.class))).thenThrow(exception);
        
        // Act & Assert
        AmazonClientException thrown = assertThrows(AmazonClientException.class, 
                () -> s3Services.downloadS3File(keyName));
        
        assertEquals("Client error", thrown.getMessage());
        verify(s3client).getObject(any(GetObjectRequest.class));
    }
    
    // uploadS3File tests
    @Test
    @DisplayName("2.5 - uploadS3File happy path")
    void uploadS3File_WhenValidFile_ShouldUploadSuccessfully() throws IOException {
        // Arrange
        String keyName = "test-upload.txt";
        byte[] fileContent = "Test upload content".getBytes();
        
        when(mockFile.getSize()).thenReturn((long) fileContent.length);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
        doNothing().when(s3client).putObject(any(PutObjectRequest.class));
        
        // Act
        s3Services.uploadS3File(keyName, mockFile);
        
        // Assert
        verify(s3client).putObject(any(PutObjectRequest.class));
    }
    
    @Test
    @DisplayName("2.6 - uploadS3File with IOException")
    void uploadS3File_WhenIOExceptionOccurs_ShouldHandleException() throws IOException {
        // Arrange
        String keyName = "io-error-upload.txt";
        
        when(mockFile.getSize()).thenReturn(100L);
        when(mockFile.getInputStream()).thenThrow(new IOException("Input stream error"));
        
        // Act
        s3Services.uploadS3File(keyName, mockFile);
        
        // Assert
        verify(s3client, never()).putObject(any(PutObjectRequest.class));
    }
    
    @Test
    @DisplayName("2.7 - uploadS3File with AmazonServiceException")
    void uploadS3File_WhenAmazonServiceExceptionOccurs_ShouldThrowException() throws IOException {
        // Arrange
        String keyName = "service-error-upload.txt";
        AmazonServiceException exception = new AmazonServiceException("Service error");
        
        when(mockFile.getSize()).thenReturn(100L);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        doThrow(exception).when(s3client).putObject(any(PutObjectRequest.class));
        
        // Act & Assert
        AmazonServiceException thrown = assertThrows(AmazonServiceException.class, 
                () -> s3Services.uploadS3File(keyName, mockFile));
        
        assertEquals("Service error", thrown.getMessage());
    }
    
    @Test
    @DisplayName("2.8 - uploadS3File with AmazonClientException")
    void uploadS3File_WhenAmazonClientExceptionOccurs_ShouldThrowException() throws IOException {
        // Arrange
        String keyName = "client-error-upload.txt";
        AmazonClientException exception = new AmazonClientException("Client error");
        
        when(mockFile.getSize()).thenReturn(100L);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        doThrow(exception).when(s3client).putObject(any(PutObjectRequest.class));
        
        // Act & Assert
        AmazonClientException thrown = assertThrows(AmazonClientException.class, 
                () -> s3Services.uploadS3File(keyName, mockFile));
        
        assertEquals("Client error", thrown.getMessage());
    }
    
    // listS3Files tests
    @Test
    @DisplayName("2.9 - listS3Files with multiple objects")
    void listS3Files_WithMultipleObjects_ShouldReturnFileList() {
        // Arrange
        List<S3ObjectSummary> firstBatchSummaries = new ArrayList<>();
        S3ObjectSummary file1 = new S3ObjectSummary();
        file1.setKey("file1.txt");
        S3ObjectSummary file2 = new S3ObjectSummary();
        file2.setKey("file2.txt");
        firstBatchSummaries.add(file1);
        firstBatchSummaries.add(file2);
        
        ObjectListing firstBatch = mock(ObjectListing.class);
        ObjectListing emptyBatch = mock(ObjectListing.class);
        
        when(s3client.listObjects(any(ListObjectsRequest.class))).thenReturn(firstBatch);
        when(firstBatch.getObjectSummaries()).thenReturn(firstBatchSummaries);
        when(firstBatch.isTruncated()).thenReturn(true);
        when(s3client.listNextBatchOfObjects(firstBatch)).thenReturn(emptyBatch);
        when(emptyBatch.getObjectSummaries()).thenReturn(new ArrayList<>());
        
        // Act
        List<String> result = s3Services.listS3Files();
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("file1.txt", "file2.txt")));
    }
    
    @Test
    @DisplayName("2.10 - listS3Files should filter out directories")
    void listS3Files_WithDirectories_ShouldFilterThem() {
        // Arrange
        List<S3ObjectSummary> firstBatchSummaries = new ArrayList<>();
        S3ObjectSummary file = new S3ObjectSummary();
        file.setKey("file.txt");
        S3ObjectSummary directory = new S3ObjectSummary();
        directory.setKey("folder/");
        firstBatchSummaries.add(file);
        firstBatchSummaries.add(directory);
        
        ObjectListing firstBatch = mock(ObjectListing.class);
        ObjectListing emptyBatch = mock(ObjectListing.class);
        
        when(s3client.listObjects(any(ListObjectsRequest.class))).thenReturn(firstBatch);
        when(firstBatch.getObjectSummaries()).thenReturn(firstBatchSummaries);
        when(firstBatch.isTruncated()).thenReturn(true);
        when(s3client.listNextBatchOfObjects(firstBatch)).thenReturn(emptyBatch);
        when(emptyBatch.getObjectSummaries()).thenReturn(new ArrayList<>());
        
        // Act
        List<String> result = s3Services.listS3Files();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("file.txt", result.get(0));
    }
    
    @Test
    @DisplayName("2.11 - listS3Files with empty bucket")
    void listS3Files_WithEmptyBucket_ShouldReturnEmptyList() {
        // Arrange
        ObjectListing emptyBatch = mock(ObjectListing.class);
        when(s3client.listObjects(any(ListObjectsRequest.class))).thenReturn(emptyBatch);
        when(emptyBatch.getObjectSummaries()).thenReturn(new ArrayList<>());
        
        // Act
        List<String> result = s3Services.listS3Files();
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    // deleteFile test
    @Test
    @DisplayName("deleteFile should delete object from S3")
    void deleteFile_ShouldDeleteObjectFromS3() {
        // Arrange
        String keyName = "file-to-delete.txt";
        doNothing().when(s3client).deleteObject(any(DeleteObjectRequest.class));
        
        // Act
        s3Services.deleteFile(keyName);
        
        // Assert
        verify(s3client).deleteObject(any(DeleteObjectRequest.class));
    }
}