package com.d7knight.anrwatchdog

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for Appetize.io upload functionality.
 * These tests simulate the upload logic used in the GitHub Actions workflow.
 */
class AppetizeUploadTest {

    private lateinit var mockAppetizeApi: MockAppetizeApi

    @Before
    fun setup() {
        mockAppetizeApi = MockAppetizeApi()
    }

    @Test
    fun `test upload creates new app when no public key provided`() {
        // Arrange: Prepare upload request
        val apkPath = "path/to/demo.apk"
        val uploadRequest = AppetizeUploadRequest(
            apkPath = apkPath,
            platform = "android",
            publicKey = null,
            apiToken = "test-token"
        )

        // Act: Upload APK
        val response = mockAppetizeApi.uploadApk(uploadRequest)

        // Assert: Should create new app
        assertTrue(response.success, "Upload should succeed")
        assertNotNull(response.publicKey, "Should return public key")
        assertTrue(response.publicKey.startsWith("test_"), "Public key should have correct format")
        assertNotNull(response.appUrl, "Should return app URL")
        assertTrue(response.isNewApp, "Should indicate this is a new app")
    }

    @Test
    fun `test upload updates existing app when public key provided`() {
        // Arrange: Prepare upload request with existing public key
        val existingKey = "existing_public_key_123"
        val uploadRequest = AppetizeUploadRequest(
            apkPath = "path/to/demo.apk",
            platform = "android",
            publicKey = existingKey,
            apiToken = "test-token"
        )

        // Act: Upload APK
        val response = mockAppetizeApi.uploadApk(uploadRequest)

        // Assert: Should update existing app
        assertTrue(response.success, "Upload should succeed")
        assertEquals(existingKey, response.publicKey, "Should return same public key")
        assertNotNull(response.appUrl, "Should return app URL")
        assertFalse(response.isNewApp, "Should indicate this is an update")
    }

    @Test
    fun `test upload fails with invalid API token`() {
        // Arrange: Prepare upload request with invalid token
        val uploadRequest = AppetizeUploadRequest(
            apkPath = "path/to/demo.apk",
            platform = "android",
            publicKey = null,
            apiToken = "" // Invalid empty token
        )

        // Act: Upload APK
        val response = mockAppetizeApi.uploadApk(uploadRequest)

        // Assert: Should fail
        assertFalse(response.success, "Upload should fail with invalid token")
        assertEquals("Invalid API token", response.errorMessage, "Should return appropriate error")
    }

    @Test
    fun `test upload fails with empty APK path`() {
        // Arrange: Prepare upload request with empty APK path
        val uploadRequest = AppetizeUploadRequest(
            apkPath = "",
            platform = "android",
            publicKey = null,
            apiToken = "test-token"
        )

        // Act: Upload APK
        val response = mockAppetizeApi.uploadApk(uploadRequest)

        // Assert: Should fail
        assertFalse(response.success, "Upload should fail with empty APK path")
        assertEquals("APK path is required", response.errorMessage, "Should return appropriate error")
    }

    @Test
    fun `test upload generates correct app URL`() {
        // Arrange: Prepare upload request
        val uploadRequest = AppetizeUploadRequest(
            apkPath = "path/to/demo.apk",
            platform = "android",
            publicKey = null,
            apiToken = "test-token"
        )

        // Act: Upload APK
        val response = mockAppetizeApi.uploadApk(uploadRequest)

        // Assert: Verify app URL format
        assertTrue(response.success, "Upload should succeed")
        assertNotNull(response.appUrl, "Should return app URL")
        assertTrue(
            response.appUrl.startsWith("https://appetize.io/app/"),
            "App URL should have correct format"
        )
        assertTrue(
            response.appUrl.contains(response.publicKey),
            "App URL should contain public key"
        )
    }

    @Test
    fun `test upload generates correct embed URL`() {
        // Arrange: Prepare upload request
        val uploadRequest = AppetizeUploadRequest(
            apkPath = "path/to/demo.apk",
            platform = "android",
            publicKey = null,
            apiToken = "test-token"
        )

        // Act: Upload APK
        val response = mockAppetizeApi.uploadApk(uploadRequest)

        // Assert: Verify embed URL format
        assertTrue(response.success, "Upload should succeed")
        assertNotNull(response.embedUrl, "Should return embed URL")
        assertTrue(
            response.embedUrl.startsWith("https://appetize.io/embed/"),
            "Embed URL should have correct format"
        )
        assertTrue(
            response.embedUrl.contains(response.publicKey),
            "Embed URL should contain public key"
        )
    }

    @Test
    fun `test validate API token succeeds with valid token`() {
        // Arrange: Valid API token
        val apiToken = "valid-token-123"

        // Act: Validate token
        val isValid = mockAppetizeApi.validateApiToken(apiToken)

        // Assert: Should be valid
        assertTrue(isValid, "Valid API token should pass validation")
    }

    @Test
    fun `test validate API token fails with empty token`() {
        // Arrange: Empty API token
        val apiToken = ""

        // Act: Validate token
        val isValid = mockAppetizeApi.validateApiToken(apiToken)

        // Assert: Should be invalid
        assertFalse(isValid, "Empty API token should fail validation")
    }

    @Test
    fun `test validate API token fails with whitespace-only token`() {
        // Arrange: Whitespace-only API token
        val apiToken = "   "

        // Act: Validate token
        val isValid = mockAppetizeApi.validateApiToken(apiToken)

        // Assert: Should be invalid
        assertFalse(isValid, "Whitespace-only API token should fail validation")
    }

    @Test
    fun `test upload request validation`() {
        // Arrange & Act & Assert: Test various validation scenarios
        
        // Valid request
        val validRequest = AppetizeUploadRequest(
            apkPath = "path/to/demo.apk",
            platform = "android",
            publicKey = null,
            apiToken = "test-token"
        )
        assertTrue(validRequest.isValid(), "Valid request should pass validation")

        // Invalid request - empty APK path
        val invalidRequest1 = AppetizeUploadRequest(
            apkPath = "",
            platform = "android",
            publicKey = null,
            apiToken = "test-token"
        )
        assertFalse(invalidRequest1.isValid(), "Request with empty APK path should fail validation")

        // Invalid request - empty API token
        val invalidRequest2 = AppetizeUploadRequest(
            apkPath = "path/to/demo.apk",
            platform = "android",
            publicKey = null,
            apiToken = ""
        )
        assertFalse(invalidRequest2.isValid(), "Request with empty API token should fail validation")

        // Invalid request - invalid platform
        val invalidRequest3 = AppetizeUploadRequest(
            apkPath = "path/to/demo.apk",
            platform = "windows", // Invalid platform
            publicKey = null,
            apiToken = "test-token"
        )
        assertFalse(invalidRequest3.isValid(), "Request with invalid platform should fail validation")
    }

    // Mock classes and data structures

    data class AppetizeUploadRequest(
        val apkPath: String,
        val platform: String,
        val publicKey: String?,
        val apiToken: String
    ) {
        fun isValid(): Boolean {
            return apkPath.isNotBlank() &&
                   apiToken.isNotBlank() &&
                   (platform == "android" || platform == "ios")
        }
    }

    data class AppetizeUploadResponse(
        val success: Boolean,
        val publicKey: String = "",
        val appUrl: String = "",
        val embedUrl: String = "",
        val isNewApp: Boolean = false,
        val errorMessage: String? = null
    )

    class MockAppetizeApi {
        fun uploadApk(request: AppetizeUploadRequest): AppetizeUploadResponse {
            // Validate API token
            if (!validateApiToken(request.apiToken)) {
                return AppetizeUploadResponse(
                    success = false,
                    errorMessage = "Invalid API token"
                )
            }

            // Validate APK path
            if (request.apkPath.isBlank()) {
                return AppetizeUploadResponse(
                    success = false,
                    errorMessage = "APK path is required"
                )
            }

            // Validate platform
            if (request.platform != "android" && request.platform != "ios") {
                return AppetizeUploadResponse(
                    success = false,
                    errorMessage = "Invalid platform"
                )
            }

            // Simulate upload
            val publicKey = request.publicKey ?: generatePublicKey()
            val isNewApp = request.publicKey == null

            return AppetizeUploadResponse(
                success = true,
                publicKey = publicKey,
                appUrl = "https://appetize.io/app/$publicKey",
                embedUrl = "https://appetize.io/embed/$publicKey",
                isNewApp = isNewApp
            )
        }

        fun validateApiToken(token: String): Boolean {
            return token.isNotBlank()
        }

        private var idCounter = 0

        private fun generatePublicKey(): String {
            return "test_mock_${++idCounter}"
        }
    }
}
