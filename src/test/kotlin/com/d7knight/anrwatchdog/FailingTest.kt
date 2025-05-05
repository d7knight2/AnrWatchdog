import kotlin.test.Test
import kotlin.test.assertTrue

class FailingTest {
    @Test
    fun testShouldFail() {
        assertTrue(false, "This test is designed to fail.")
    }
}