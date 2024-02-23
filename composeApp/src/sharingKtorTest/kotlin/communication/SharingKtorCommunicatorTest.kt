package communication

import kotlin.test.Test
import kotlin.test.assertEquals

class SharingKtorCommunicatorTest {

    @Test
    fun doAsynchronouslyTest() {
        var string = ""

        string += "a"

        doAsynchronously {
            string += "b"
        }

        string += "c"

        val want = "ac"
        val have = string

        assertEquals(want, have)
    }
}