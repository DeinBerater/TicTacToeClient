package communication

import io.ktor.client.engine.js.Js
import kotlin.test.Test
import kotlin.test.assertEquals

class JsCommunicatorTest {

    @Test
    fun getEngine() {
        val want = Js
        val have = getCommunicatorEngine()

        assertEquals(want, have)
    }
}