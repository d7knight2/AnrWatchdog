import kotlin.test.Test
import kotlin.test.assertTrue

class FailingTest {
    @Test
    fun testShouldPass() {
        assertTrue(true, "This test should pass.")
    }
}