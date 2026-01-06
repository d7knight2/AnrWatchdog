package com.d7knight.anrwatchdog

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for APK discovery functionality.
 * These tests simulate the APK discovery logic used in the GitHub Actions workflow.
 */
class ApkDiscoveryTest {

    private lateinit var tempDir: File
    private lateinit var apkOutputDir: File

    @Before
    fun setup() {
        // Create temporary directories for testing
        tempDir = createTempDir("apk-discovery-test")
        apkOutputDir = File(tempDir, "demoapp/build/outputs/apk/debug").apply {
            mkdirs()
        }
    }

    @After
    fun tearDown() {
        // Clean up temporary directories
        tempDir.deleteRecursively()
    }

    @Test
    fun `test APK discovery finds APK file successfully`() {
        // Arrange: Create a mock APK file
        val apkFile = File(apkOutputDir, "demoapp-debug.apk")
        apkFile.writeText("Mock APK content")

        // Act: Discover APK files
        val discoveredApks = discoverApkFiles(apkOutputDir)

        // Assert: Verify APK was found
        assertNotNull(discoveredApks, "Should find APK files")
        assertTrue(discoveredApks.isNotEmpty(), "Should find at least one APK")
        assertEquals(1, discoveredApks.size, "Should find exactly one APK")
        assertEquals("demoapp-debug.apk", discoveredApks[0].name, "APK name should match")
    }

    @Test
    fun `test APK discovery handles missing directory`() {
        // Arrange: Use non-existent directory
        val nonExistentDir = File(tempDir, "non-existent")

        // Act: Discover APK files
        val discoveredApks = discoverApkFiles(nonExistentDir)

        // Assert: Should return empty list
        assertNotNull(discoveredApks, "Should return non-null result")
        assertTrue(discoveredApks.isEmpty(), "Should return empty list for non-existent directory")
    }

    @Test
    fun `test APK discovery handles empty directory`() {
        // Arrange: Empty directory (no APK files)

        // Act: Discover APK files
        val discoveredApks = discoverApkFiles(apkOutputDir)

        // Assert: Should return empty list
        assertNotNull(discoveredApks, "Should return non-null result")
        assertTrue(discoveredApks.isEmpty(), "Should return empty list for empty directory")
    }

    @Test
    fun `test APK discovery finds multiple APK files`() {
        // Arrange: Create multiple mock APK files
        val apk1 = File(apkOutputDir, "demoapp-debug.apk")
        val apk2 = File(apkOutputDir, "demoapp-release.apk")
        apk1.writeText("Mock APK 1 content")
        apk2.writeText("Mock APK 2 content")

        // Act: Discover APK files
        val discoveredApks = discoverApkFiles(apkOutputDir)

        // Assert: Verify both APKs were found
        assertNotNull(discoveredApks, "Should find APK files")
        assertEquals(2, discoveredApks.size, "Should find two APK files")
        assertTrue(
            discoveredApks.any { it.name == "demoapp-debug.apk" },
            "Should find debug APK"
        )
        assertTrue(
            discoveredApks.any { it.name == "demoapp-release.apk" },
            "Should find release APK"
        )
    }

    @Test
    fun `test APK discovery ignores non-APK files`() {
        // Arrange: Create mix of APK and non-APK files
        val apkFile = File(apkOutputDir, "demoapp-debug.apk")
        val txtFile = File(apkOutputDir, "readme.txt")
        val xmlFile = File(apkOutputDir, "output.xml")
        apkFile.writeText("Mock APK content")
        txtFile.writeText("Text file content")
        xmlFile.writeText("<xml>XML content</xml>")

        // Act: Discover APK files
        val discoveredApks = discoverApkFiles(apkOutputDir)

        // Assert: Should only find APK file
        assertNotNull(discoveredApks, "Should find APK files")
        assertEquals(1, discoveredApks.size, "Should find only one APK file")
        assertEquals("demoapp-debug.apk", discoveredApks[0].name, "Should find only the APK file")
    }

    @Test
    fun `test APK file validation succeeds for valid file`() {
        // Arrange: Create a valid mock APK file
        val apkFile = File(apkOutputDir, "demoapp-debug.apk")
        apkFile.writeText("Mock APK content")

        // Act: Validate APK file
        val isValid = validateApkFile(apkFile)

        // Assert: Should be valid
        assertTrue(isValid, "Valid APK file should pass validation")
    }

    @Test
    fun `test APK file validation fails for non-existent file`() {
        // Arrange: Non-existent file
        val nonExistentFile = File(apkOutputDir, "non-existent.apk")

        // Act: Validate APK file
        val isValid = validateApkFile(nonExistentFile)

        // Assert: Should fail validation
        assertFalse(isValid, "Non-existent file should fail validation")
    }

    @Test
    fun `test APK file validation fails for empty file`() {
        // Arrange: Create empty APK file
        val emptyApkFile = File(apkOutputDir, "empty.apk")
        emptyApkFile.createNewFile()

        // Act: Validate APK file
        val isValid = validateApkFile(emptyApkFile)

        // Assert: Should fail validation (empty file is invalid)
        assertFalse(isValid, "Empty APK file should fail validation")
    }

    @Test
    fun `test get APK file size`() {
        // Arrange: Create mock APK with known content
        val apkFile = File(apkOutputDir, "demoapp-debug.apk")
        val content = "Mock APK content with some data"
        apkFile.writeText(content)

        // Act: Get file size
        val size = getApkFileSize(apkFile)

        // Assert: Size should match content length
        assertEquals(content.length.toLong(), size, "File size should match content length")
    }

    // Helper functions that simulate the workflow logic

    private fun discoverApkFiles(directory: File): List<File> {
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }
        return directory.listFiles { file ->
            file.isFile && file.extension == "apk"
        }?.toList() ?: emptyList()
    }

    private fun validateApkFile(file: File): Boolean {
        return file.exists() && file.isFile && file.canRead() && file.length() > 0
    }

    private fun getApkFileSize(file: File): Long {
        return if (file.exists() && file.isFile) file.length() else 0L
    }
}
