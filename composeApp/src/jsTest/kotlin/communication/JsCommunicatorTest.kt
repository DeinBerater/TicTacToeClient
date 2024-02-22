package communication

import kotlin.test.Test
import kotlin.test.assertEquals

class JsCommunicatorTest {

    @Test
    fun doAsynchronouslyTest() {
        var string = ""

        string += "a"

        doAsynchronously {
            string += "b"
        }

        string += "c"

        val want = "acb"
        val have = string

        assertEquals(want, have)
        assertEquals("want", "have")
    }
}