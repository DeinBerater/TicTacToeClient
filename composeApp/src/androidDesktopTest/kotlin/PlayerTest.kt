import communication.MockCommunicator
import communication.createCommunicator
import game.Game
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PlayerTest {

    @Before
    fun setUp() {
        mockkStatic("communication.BaseCommunicator")
        mockkStatic("communication.Communicator_sharingKtorKt")
    }

    @After
    fun tearDown() {
        // Disable static mocking after the test to avoid side effects
        unmockkStatic("communication.BaseCommunicator")
        unmockkStatic("communication.Communicator_sharingKtorKt")
    }

    @Test
    fun mockCommunicator(): Unit = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val communicator = createCommunicator()

        var changed = false
        launch {
            communicator.connectWithWebsocket()
            changed = true
        }
        assertFalse(changed)
    }

    @Test
    fun playerCreated() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val sut = Player(this)

        val have = sut.game.gameCode
        val want = Game().gameCode

        assertEquals(want, have)
    }
}
