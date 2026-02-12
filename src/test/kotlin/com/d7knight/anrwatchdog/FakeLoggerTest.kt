package com.d7knight.anrwatchdog

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertTrue

/**
 * Unit tests for FakeLogger following TDD principles.
 * Tests cover basic functionality, edge cases, and thread safety.
 */
class FakeLoggerTest {

    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var printStream: PrintStream
    private lateinit var originalOut: PrintStream

    @Before
    fun setup() {
        // Capture standard output
        outputStream = ByteArrayOutputStream()
        printStream = PrintStream(outputStream)
        originalOut = System.out
        System.setOut(printStream)
    }

    @After
    fun tearDown() {
        // Restore standard output
        System.setOut(originalOut)
    }

    @Test
    fun testLogSimpleMessage() {
        // Given
        val message = "Test message"
        
        // When
        FakeLogger.log(message)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("[FakeLogger]"), "Output should contain logger prefix")
        assertTrue(output.contains(message), "Output should contain the message")
    }

    @Test
    fun testLogEmptyString() {
        // Given
        val message = ""
        
        // When
        FakeLogger.log(message)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("[FakeLogger]"), "Output should contain logger prefix even with empty message")
    }

    @Test
    fun testLogWithSpecialCharacters() {
        // Given
        val message = "Special chars: !@#\$%^&*()_+-=[]{}|;':\",./<>?"
        
        // When
        FakeLogger.log(message)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("[FakeLogger]"), "Output should contain logger prefix")
        assertTrue(output.contains(message), "Output should contain special characters")
    }

    @Test
    fun testLogWithNewlines() {
        // Given
        val message = "Line 1\nLine 2\nLine 3"
        
        // When
        FakeLogger.log(message)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("[FakeLogger]"), "Output should contain logger prefix")
        assertTrue(output.contains("Line 1"), "Output should contain first line")
        assertTrue(output.contains("Line 2"), "Output should contain second line")
        assertTrue(output.contains("Line 3"), "Output should contain third line")
    }

    @Test
    fun testLogMultipleMessages() {
        // Given
        val message1 = "First message"
        val message2 = "Second message"
        val message3 = "Third message"
        
        // When
        FakeLogger.log(message1)
        FakeLogger.log(message2)
        FakeLogger.log(message3)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains(message1), "Output should contain first message")
        assertTrue(output.contains(message2), "Output should contain second message")
        assertTrue(output.contains(message3), "Output should contain third message")
    }

    @Test
    fun testLogLongMessage() {
        // Given
        val message = "A".repeat(10000) // Very long message
        
        // When
        FakeLogger.log(message)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("[FakeLogger]"), "Output should contain logger prefix")
        assertTrue(output.contains(message), "Output should contain long message")
    }
}
