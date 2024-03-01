package communication

import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidDesktopCommunicatorTest {

    @Test
    fun getEngine() {
        val want = CIO
        val have = getCommunicatorEngine()

        assertEquals(want, have)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun doAsynchronouslyTest() = runBlocking {
//        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
//        Dispatchers.setMain(testDispatcher)

        var string = ""

        string += "a"

        doAsynchronously {
            delay(100L)
            string += "b"
        }

        string += "c"

        delay(300L)


        val want = "acb"
        val have = string

        assertEquals(want, have)

//        Dispatchers.resetMain()

    }
}