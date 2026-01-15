package com.d7knight.anrwatchdog

import org.junit.Test
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Test suite for FakeLogger utility class.
 */
class FakeLoggerTest {

    /**
     * Tests basic logging functionality.
     */
    @Test
    fun testLogMessage() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        FakeLogger.log("Test message")

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("[FakeLogger]"), "Output should contain logger prefix")
        assertTrue(output.contains("Test message"), "Output should contain the message")
    }

    /**
     * Tests logging empty string.
     */
    @Test
    fun testLogEmptyString() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        FakeLogger.log("")

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("[FakeLogger]"), "Output should contain logger prefix even for empty message")
    }

    /**
     * Tests logging with special characters.
     */
    @Test
    fun testLogSpecialCharacters() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        val specialMessage = "Special chars: @#$%^&*()[]{}!~`"
        FakeLogger.log(specialMessage)

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains(specialMessage), "Special characters should be preserved")
    }

    /**
     * Tests logging with newlines.
     */
    @Test
    fun testLogMultilineMessage() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        val multilineMessage = "Line 1\nLine 2\nLine 3"
        FakeLogger.log(multilineMessage)

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("Line 1"), "Should contain first line")
        assertTrue(output.contains("Line 2"), "Should contain second line")
        assertTrue(output.contains("Line 3"), "Should contain third line")
    }

    /**
     * Tests multiple consecutive log calls.
     */
    @Test
    fun testMultipleLogCalls() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        FakeLogger.log("Message 1")
        FakeLogger.log("Message 2")
        FakeLogger.log("Message 3")

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("Message 1"), "Should contain first message")
        assertTrue(output.contains("Message 2"), "Should contain second message")
        assertTrue(output.contains("Message 3"), "Should contain third message")
        
        // Count occurrences of the prefix
        val prefixCount = output.split("[FakeLogger]").size - 1
        assertTrue(prefixCount == 3, "Should have 3 log prefixes")
    }

    /**
     * Tests logging with Unicode characters.
     */
    @Test
    fun testLogUnicodeCharacters() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        val unicodeMessage = "Unicode: 你好世界 🚀 ñ ü é"
        FakeLogger.log(unicodeMessage)

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("Unicode:"), "Should contain Unicode message")
    }

    /**
     * Tests logging very long message.
     */
    @Test
    fun testLogLongMessage() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        val longMessage = "A".repeat(10000)
        FakeLogger.log(longMessage)

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("[FakeLogger]"), "Should contain logger prefix")
        assertTrue(output.contains("AAAA"), "Should contain part of the long message")
    }
}
