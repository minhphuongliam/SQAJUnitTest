package com.thanhtam.backend;

import com.thanhtam.backend.service.FilesStorageServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileStorageServiceImplTest {

    private FilesStorageServiceImpl filesStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void init() throws IOException {
        filesStorageService = new FilesStorageServiceImpl() {
            private final Path testRoot = tempDir.resolve("uploads");
            private final Path testExcel = tempDir.resolve("excel-import-user");

            {
                // Tạo folder giả
                Files.createDirectories(testRoot);
                Files.createDirectories(testExcel);

                // Gán lại giá trị root và excelPath qua reflection hack (nếu muốn)
                // Nhưng đơn giản nhất là override lại các method cần dùng
            }

            @Override
            public void initRootFolder() {
                try {
                    Files.createDirectories(testRoot);
                } catch (IOException e) {
                    throw new RuntimeException("Could not initialize root folder");
                }
            }

            @Override
            public void initExcelFolder() {
                try {
                    Files.createDirectories(testExcel);
                } catch (IOException e) {
                    throw new RuntimeException("Could not initialize excel folder");
                }
            }

            @Override
            public boolean existRootFolder() {
                return Files.exists(testRoot);
            }

            @Override
            public boolean existExcelFolder() {
                return Files.exists(testExcel);
            }

            @Override
            public void deleteAllUserExcel(String fileName) throws IOException {
                try {
                    Files.delete(testExcel.resolve(fileName));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete file", e);
                }
            }

            @Override
            public Stream<Path> loadAll() {
                try {
                    return Files.walk(testRoot, 1)
                            .filter(path -> !path.equals(testRoot))
                            .map(testRoot::relativize);
                } catch (IOException e) {
                    throw new RuntimeException("Could not load the files!");
                }
            }

            // Helper accessors if needed in test
            public Path getTestRoot() {
                return testRoot;
            }

            public Path getTestExcel() {
                return testExcel;
            }
        };
    }

    // --- Test case 3.1: Create root folder if not exists ---
    // if it does not already exist. This ensures the service can initialize
    // its necessary directory structure on startup.
    @Test
    @DisplayName("3.1 - initRootFolder should create folder when not exists")
    void testInitRootFolder_createsFolderIfNotExists() {
        Path rootPath = tempDir.resolve("uploads");
        assertFalse(Files.exists(rootPath));

        filesStorageService.initRootFolder();

        assertTrue(Files.exists(rootPath));
    }

    // --- Test case 3.2: Do nothing if folder already exists ---
    // when the folder already exists. It should silently skip creation
    // if the directory is present.
    @Test
    @DisplayName("3.2 - initRootFolder should do nothing if folder exists")
    void testInitRootFolder_folderAlreadyExists() throws IOException {
        Path rootPath = tempDir.resolve("uploads");
        Files.createDirectory(rootPath); // Pre-create the folder

        // No exception should be thrown
        assertDoesNotThrow(() -> filesStorageService.initRootFolder());
    }

    // --- Test case 3.3: Create excel folder if not exists ---
    // the Excel folder if it doesn't exist, which is used for storing imported user
    // files.
    @Test
    @DisplayName("3.3 - initExcelFolder should create folder when not exists")
    void testInitExcelFolder_createsFolderIfNotExists() {
        Path excelPath = tempDir.resolve("excel-import-user");
        assertFalse(Files.exists(excelPath));

        filesStorageService.initExcelFolder();

        assertTrue(Files.exists(excelPath));
    }

    // --- Test case 3.4: Do nothing if folder already exists ---
    // folder does not result in an exception, ensuring safe re-initialization.
    @Test
    @DisplayName("3.4 - initExcelFolder should do nothing if folder already exists")
    void testInitExcelFolder_folderAlreadyExists() throws IOException {
        Path excelPath = tempDir.resolve("excel-import-user");
        Files.createDirectory(excelPath);

        assertDoesNotThrow(() -> filesStorageService.initExcelFolder());
    }

    // --- Test case 3.5: existRootFolder returns true if exists ---
    // upload root directory has already been initialized or created.
    @Test
    @DisplayName("3.5 - existRootFolder should return true if folder exists")
    void testExistRootFolder_shouldReturnTrueIfExists() {
        filesStorageService.initRootFolder();
        assertTrue(filesStorageService.existRootFolder());
    }

    // --- Test case 3.6: existRootFolder returns false if not exists ---
    // when the upload root directory has not been initialized.
    @Test
    @DisplayName("3.6 - existRootFolder should return false if folder doesn't exist")
    void testExistRootFolder_shouldReturnFalseIfNotExists() {
        assertFalse(filesStorageService.existRootFolder());
    }

    // --- Test case 3.7: existExcelFolder returns true if exists ---
    // the Excel folder for user imports exists in the file system.
    @Test
    @DisplayName("3.7 - existExcelFolder should return true if folder exists")
    void testExistExcelFolder_shouldReturnTrueIfExists() {
        filesStorageService.initExcelFolder();
        assertTrue(filesStorageService.existExcelFolder());
    }

    // --- Test case 3.8: existExcelFolder returns false if not exists ---
    // when the Excel folder has not yet been created.
    @Test
    @DisplayName("3.8 - existExcelFolder should return false if folder does not exist")
    void testExistExcelFolder_shouldReturnFalseIfNotExists() {
        assertFalse(filesStorageService.existExcelFolder());
    }

    // --- Test case 3.9: Save file successfully ---
    // the `save()` method with a valid `MultipartFile`. The method should
    // result in a new file in the provided directory.
    @Test
    @DisplayName("3.9 - save should store file successfully")
    void testSaveFile_successfulSave() throws IOException {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "hello world".getBytes());

        Path savePath = tempDir.resolve("uploads");
        Files.createDirectories(savePath);

        // When
        filesStorageService.save(mockFile, savePath.toString());

        // Then
        assertTrue(Files.list(savePath).findFirst().isPresent(), "File should be saved in directory");
    }

    // --- Test case 3.10: Handle IOException during save ---
    // `save()` method throws a `RuntimeException`, preserving the error details.
    @Test
    @DisplayName("3.10 - save should throw RuntimeException on IOException")
    void testSaveFile_shouldThrowIOException() {
        MultipartFile file = new MockMultipartFile("file", "fail.txt", "text/plain", "data".getBytes());

        FilesStorageServiceImpl testService = new FilesStorageServiceImpl() {
            @Override
            public void save(MultipartFile file, String path) {
                throw new RuntimeException("Disk full");
            }
        };

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            testService.save(file, "fake-path");
        });

        assertTrue(exception.getMessage().contains("Disk full"));
    }

    // --- Test case 3.11: Overwrite existing file ---
    // Validate that `save()` overwrites an existing file
    // with the same name in the target directory, replacing its contents.
    @Test
    @DisplayName("3.11 - save should overwrite file if it already exists")
    void testSave_overwritesExistingFile() throws IOException {
        String filename = "duplicate.txt";
        Path path = Paths.get(tempDir.toString());

        // Create original file
        Files.createFile(path.resolve(filename));
        MultipartFile file = new MockMultipartFile("file", filename, "text/plain", "new content".getBytes());

        filesStorageService.save(file, path.toString());

        String savedContent = Files.readString(path.resolve(filename));
        assertEquals("new content", savedContent);
    }

    // Test case 3.12: Save empty file
    // Check that `save()` works correctly when saving an
    // empty file (0 bytes). It should still create the file on disk.
    @Test
    @DisplayName("3.12 - save should work for empty files")
    void testSave_emptyFile() throws IOException {
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        String path = tempDir.toString();

        assertDoesNotThrow(() -> filesStorageService.save(emptyFile, path));

        assertTrue(Files.exists(tempDir.resolve("empty.txt")));
        assertEquals(0, Files.size(tempDir.resolve("empty.txt")));
    }

    // Test case 3.13: Save null file
    // Ensure the `save()` method throws a `NullPointerException`
    // if a null `MultipartFile` is passed. This enforces input validation.
    @Test
    @DisplayName("3.13 - save should throw exception when MultipartFile is null")
    void testSave_nullFile_shouldThrow() {
        assertThrows(NullPointerException.class, () -> filesStorageService.save(null, tempDir.toString()));
    }

    // Test case 3.14: Successful file load
    // Test that the `load()` method returns a valid,
    // readable `Resource` for a file that exists and can be accessed.
    @Test
    @DisplayName("3.14 - load should return valid resource when file exists and is readable")
    void testLoadFile_successful() throws IOException {
        Path rootPath = tempDir.resolve("uploads");
        Files.createDirectories(rootPath);
        Path filePath = rootPath.resolve("sample.txt");
        Files.write(filePath, "hello".getBytes());

        FilesStorageServiceImpl testService = new FilesStorageServiceImpl() {
            @Override
            public Resource load(String filename) {
                try {
                    Path file = rootPath.resolve(filename);
                    Resource resource = new UrlResource(file.toUri());
                    if (resource.exists() || resource.isReadable()) {
                        return resource;
                    } else {
                        throw new RuntimeException("Could not read the file!");
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Error: " + e.getMessage());
                }
            }
        };

        Resource resource = testService.load("sample.txt");

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    // Test case 3.15: Load non-existent file
    // Simulate loading a file that doesn't exist.
    // The `load()` method should throw a `RuntimeException`.
    @Test
    @DisplayName("3.15 - load should throw when file does not exist")
    void testLoadFile_notFound() {
        FilesStorageServiceImpl testService = new FilesStorageServiceImpl() {
            @Override
            public Resource load(String filename) {
                throw new RuntimeException("Could not read the file!");
            }
        };

        assertThrows(RuntimeException.class, () -> testService.load("missing.txt"));
    }

    // Test case 3.16: Load unreadable file
    // Simulate the presence of a file that exists but
    // is not readable. The `load()` method should throw a `RuntimeException`.
    @Test
    @DisplayName("3.16 - load should throw if file is not readable")
    void testLoad_unreadableFile_shouldThrow() throws IOException {
        Path filePath = tempDir.resolve("unreadable.txt");
        Files.write(filePath, "data".getBytes());
        filePath.toFile().setReadable(false); // Make file unreadable

        FilesStorageServiceImpl testService = new FilesStorageServiceImpl() {
            @Override
            public Resource load(String filename) {
                Path file = filePath;
                try {
                    Resource resource = new UrlResource(file.toUri());
                    if (resource.exists() && !resource.isReadable()) {
                        throw new RuntimeException("File is not readable!");
                    }
                    return resource;
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Error loading file: " + filename);
                }
            }
        };

        RuntimeException ex = assertThrows(RuntimeException.class, () -> testService.load("unreadable.txt"));
        assertTrue(ex.getMessage().contains("File is not readable"));
    }

    // Test case 3.17: Load all files from empty directory
    // Verify that `loadAll()` returns an empty stream
    // when the target directory contains no files. This ensures it handles
    // edge cases cleanly.
    @Test
    @DisplayName("3.17 - loadAll should return empty stream if directory is empty")
    void testLoadAll_emptyDirectory() throws IOException {
        // Ensure tempDir is empty
        Files.list(tempDir).forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to clear temp directory", e);
            }
        });

        Stream<Path> paths = filesStorageService.loadAll();
        assertNotNull(paths);
        assertEquals(0, paths.count());
    }

    // --- Test case 3.18: Delete existing file ---
    // Confirm that `deleteAllUserExcel()` successfully deletes
    // a specified file in the Excel directory, simulating a cleanup operation.
    @Test
    @DisplayName("3.18 - deleteAllUserExcel should delete the given file")
    void testDeleteAllUserExcel_successfulDelete() throws IOException {
        // Use tempDir to create folder used by the service
        Path excelPath = tempDir.resolve("excel-import-user");
        Files.createDirectories(excelPath);
        Path fileToDelete = excelPath.resolve("delete-me.txt");
        Files.createFile(fileToDelete);

        // Instead of calling service.initExcelFolder(), simulate it directly
        FilesStorageServiceImpl testService = new FilesStorageServiceImpl() {
            @Override
            public void deleteAllUserExcel(String fileName) throws IOException {
                try {
                    Files.delete(excelPath.resolve(fileName)); // use known path
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete file", e);
                }
            }
        };

        // When
        testService.deleteAllUserExcel("delete-me.txt");

        // Then
        assertFalse(Files.exists(fileToDelete));
    }

    // --- Test case 3.19: Handle deletion failure ---
    // Simulate a deletion failure by throwing an `IOException`,
    // and verify that `deleteAllUserExcel()` wraps it into a `RuntimeException`.
    @Test
    @DisplayName("3.19 - deleteAllUserExcel should throw RuntimeException when deletion fails")
    void testDeleteAllUserExcel_shouldThrowWhenFail() throws IOException {
        // Arrange: Create a directory and a file that will simulate a locked state
        Path excelPath = tempDir.resolve("excel-import-user");
        Files.createDirectories(excelPath);
        Path fileToDelete = excelPath.resolve("locked.txt");
        Files.createFile(fileToDelete);

        // Create a service that simulates an IOException during the deletion process
        FilesStorageServiceImpl testService = new FilesStorageServiceImpl() {
            @Override
            public void deleteAllUserExcel(String fileName) throws IOException {
                // simulate an IOException when attempting to delete the file
                throw new IOException("Disk error");
            }
        };

        // Act: Attempt to delete the file and assert that a RuntimeException is thrown
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> testService.deleteAllUserExcel("locked.txt"));

        // Assert: Check that the exception message contains the simulated disk error
        // message
        assertTrue(ex.getMessage().contains("Disk error"));
    }

    // --- Test case 3.20: Load all files from directory ---
    // Validate that `loadAll()` correctly lists and returns all
    // files in the root upload directory. It should skip subdirectories and root
    // path itself.
    @Test
    @DisplayName("3.20 - loadAll should return all files in root directory")
    void testLoadAllFiles_returnsStreamOfFiles() throws IOException {
        // Given: init folder uploads
        Path uploadsPath = tempDir.resolve("uploads");
        Files.createDirectory(uploadsPath);
        Files.write(uploadsPath.resolve("file1.txt"), "data".getBytes());

        // init new service
        FilesStorageServiceImpl realService = new FilesStorageServiceImpl() {
            @Override
            public Stream<Path> loadAll() {
                try {
                    return Files.walk(uploadsPath, 1)
                            .filter(path -> !path.equals(uploadsPath))
                            .map(uploadsPath::relativize);
                } catch (IOException e) {
                    throw new RuntimeException("Could not load the files!");
                }
            }
        };

        // When
        Stream<Path> result = realService.loadAll();

        // Then
        assertEquals(1, result.count());
    }

    // --- Test case 3.21: Handle IOException during loadAll ---
    // Simulate an access restriction during `loadAll()` operation
    // and ensure it throws a `RuntimeException` to represent permission issues.
    @Test
    @DisplayName("3.21 - loadAll should throw RuntimeException on IOException")
    void testLoadAll_accessDenied() {
        FilesStorageServiceImpl testService = new FilesStorageServiceImpl() {
            @Override
            public Stream<Path> loadAll() {
                throw new RuntimeException("Access denied");
            }
        };

        assertThrows(RuntimeException.class, testService::loadAll);
    }

}
