package com.d7knight.anrwatchdog

import com.d7knight.anrwatchdog.rxjava.SlowRxExperimentEnabledRepository
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.util.concurrent.TimeUnit
import java.util.concurrent.CountDownLatch

/**
 * Test suite for SlowRxExperimentEnabledRepository.
 * Tests RxJava operations with TestScheduler for deterministic timing.
 */
class SlowRxExperimentEnabledRepositoryTest {

    /**
     * Tests basic operation execution.
     */
    @Test
    fun testPerformSlowOperation() {
        val events = mutableListOf<String>()
        val latch = CountDownLatch(2)
        
        val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(1)
        
        publisher.subscribe { event ->
            events.add(event)
            latch.countDown()
        }
        
        // Advance time to trigger scheduled events
        SlowRxExperimentEnabledRepository.advanceTimeBy(100, TimeUnit.MILLISECONDS)
        
        latch.await(1, TimeUnit.SECONDS)
        
        assertTrue(events.contains("Started SlowRxExperimentJob-1"), "Should contain start event")
        assertTrue(events.contains("Finished SlowRxExperimentJob-1"), "Should contain finish event")
    }

    /**
     * Tests operation without advancing time.
     */
    @Test
    fun testOperationWithoutTimeAdvancement() {
        val events = mutableListOf<String>()
        
        val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(2)
        
        publisher.subscribe { event ->
            events.add(event)
        }
        
        // Don't advance time
        Thread.sleep(50)
        
        // Should have immediate start event but not finish event
        assertTrue(events.contains("Started SlowRxExperimentJob-2"), "Should have start event immediately")
        assertTrue(!events.contains("Finished SlowRxExperimentJob-2"), "Should not have finish event yet")
    }

    /**
     * Tests partial time advancement.
     */
    @Test
    fun testPartialTimeAdvancement() {
        val events = mutableListOf<String>()
        
        val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(3)
        
        publisher.subscribe { event ->
            events.add(event)
        }
        
        // Advance only half the required time
        SlowRxExperimentEnabledRepository.advanceTimeBy(50, TimeUnit.MILLISECONDS)
        
        Thread.sleep(50)
        
        // Should have start but not finish
        assertTrue(events.contains("Started SlowRxExperimentJob-3"), "Should have start event")
        assertTrue(!events.contains("Finished SlowRxExperimentJob-3"), "Should not have finish event with partial time")
    }

    /**
     * Tests advancing time beyond required duration.
     */
    @Test
    fun testExcessTimeAdvancement() {
        val events = mutableListOf<String>()
        val latch = CountDownLatch(2)
        
        val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(4)
        
        publisher.subscribe { event ->
            events.add(event)
            latch.countDown()
        }
        
        // Advance more time than needed
        SlowRxExperimentEnabledRepository.advanceTimeBy(500, TimeUnit.MILLISECONDS)
        
        latch.await(1, TimeUnit.SECONDS)
        
        assertEquals(2, events.size, "Should have exactly 2 events")
        assertTrue(events.contains("Started SlowRxExperimentJob-4"), "Should have start event")
        assertTrue(events.contains("Finished SlowRxExperimentJob-4"), "Should have finish event")
    }

    /**
     * Tests multiple concurrent operations.
     */
    @Test
    fun testMultipleOperations() {
        val events1 = mutableListOf<String>()
        val events2 = mutableListOf<String>()
        val latch = CountDownLatch(4)
        
        val publisher1 = SlowRxExperimentEnabledRepository.performSlowOperation(10)
        val publisher2 = SlowRxExperimentEnabledRepository.performSlowOperation(20)
        
        publisher1.subscribe { event ->
            events1.add(event)
            latch.countDown()
        }
        
        publisher2.subscribe { event ->
            events2.add(event)
            latch.countDown()
        }
        
        // Advance time for all operations
        SlowRxExperimentEnabledRepository.advanceTimeBy(100, TimeUnit.MILLISECONDS)
        
        latch.await(1, TimeUnit.SECONDS)
        
        assertTrue(events1.contains("Started SlowRxExperimentJob-10"), "Operation 1 should start")
        assertTrue(events1.contains("Finished SlowRxExperimentJob-10"), "Operation 1 should finish")
        assertTrue(events2.contains("Started SlowRxExperimentJob-20"), "Operation 2 should start")
        assertTrue(events2.contains("Finished SlowRxExperimentJob-20"), "Operation 2 should finish")
    }

    /**
     * Tests operation with different time units.
     */
    @Test
    fun testDifferentTimeUnits() {
        val events = mutableListOf<String>()
        val latch = CountDownLatch(2)
        
        val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(5)
        
        publisher.subscribe { event ->
            events.add(event)
            latch.countDown()
        }
        
        // Advance using different time unit (seconds instead of milliseconds)
        SlowRxExperimentEnabledRepository.advanceTimeBy(1, TimeUnit.SECONDS)
        
        latch.await(1, TimeUnit.SECONDS)
        
        assertEquals(2, events.size, "Should complete with time in seconds")
    }

    /**
     * Tests zero time advancement.
     */
    @Test
    fun testZeroTimeAdvancement() {
        val events = mutableListOf<String>()
        
        val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(6)
        
        publisher.subscribe { event ->
            events.add(event)
        }
        
        // Advance zero time
        SlowRxExperimentEnabledRepository.advanceTimeBy(0, TimeUnit.MILLISECONDS)
        
        Thread.sleep(50)
        
        // Should have start event but not finish
        assertTrue(events.size >= 1, "Should have at least start event")
        assertTrue(events.contains("Started SlowRxExperimentJob-6"), "Should have start event")
    }

    /**
     * Tests incremental time advancement.
     */
    @Test
    fun testIncrementalTimeAdvancement() {
        val events = mutableListOf<String>()
        val latch = CountDownLatch(2)
        
        val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(7)
        
        publisher.subscribe { event ->
            events.add(event)
            latch.countDown()
        }
        
        // Advance time incrementally
        SlowRxExperimentEnabledRepository.advanceTimeBy(50, TimeUnit.MILLISECONDS)
        Thread.sleep(50)
        
        // Check intermediate state
        assertTrue(events.contains("Started SlowRxExperimentJob-7"), "Should have start event")
        
        // Advance remaining time
        SlowRxExperimentEnabledRepository.advanceTimeBy(50, TimeUnit.MILLISECONDS)
        
        latch.await(1, TimeUnit.SECONDS)
        
        assertTrue(events.contains("Finished SlowRxExperimentJob-7"), "Should have finish event")
    }

    /**
     * Tests event ordering.
     */
    @Test
    fun testEventOrdering() {
        val events = mutableListOf<String>()
        val latch = CountDownLatch(2)
        
        val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(8)
        
        publisher.subscribe { event ->
            events.add(event)
            latch.countDown()
        }
        
        SlowRxExperimentEnabledRepository.advanceTimeBy(100, TimeUnit.MILLISECONDS)
        
        latch.await(1, TimeUnit.SECONDS)
        
        assertEquals(2, events.size, "Should have 2 events")
        assertEquals("Started SlowRxExperimentJob-8", events[0], "Start should be first")
        assertEquals("Finished SlowRxExperimentJob-8", events[1], "Finish should be second")
    }
}
