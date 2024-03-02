import communication.MockCommunicator
import communication.createCommunicator
import de.deinberater.tictactoe.garmincommunication.GarminGame
import de.deinberater.tictactoe.garmincommunication.IQAppCommunicator
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GarminGameTest {

    @Before
    fun setUp() {
        mockkStatic("communication.Communicator_sharingKtorKt")
    }

    @After
    fun tearDown() {
        // Disable static mocking after the test to avoid side effects
        unmockkStatic("communication.Communicator_sharingKtorKt")
    }

    @Test
    fun newGame() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val garminAppCommunicator = mockk<IQAppCommunicator>()
        every { garminAppCommunicator.transmitData(any()) } just runs

        val onDataReceivedSlot = slot<(Any) -> Unit>()
        every { garminAppCommunicator.setOnAppReceive(capture(onDataReceivedSlot)) } just runs

        GarminGame(garminAppCommunicator, this)


        onDataReceivedSlot.captured(listOf(0)) // Player should be created and websocket connection established.
        assertFalse(mockCommunicator.webSocketConnected)

        delay(1000L)
        assertTrue(mockCommunicator.webSocketConnected)
    }

    @Test
    fun gameStart() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val garminAppCommunicator = mockk<IQAppCommunicator>()
        val lastDataTransmittedToGarminDevice = slot<Any>()
        every { garminAppCommunicator.transmitData(capture(lastDataTransmittedToGarminDevice)) } just runs

        val onDataReceivedSlot = slot<(Any) -> Unit>()
        every { garminAppCommunicator.setOnAppReceive(capture(onDataReceivedSlot)) } just runs

        GarminGame(garminAppCommunicator, this)


        onDataReceivedSlot.captured(listOf(0)) // Player should be created and websocket connection established.

        delay(1000L)

        // Server sends game code 'AAAAA'
        val welcomeBytes = byteArrayOf(0b00000000, 0b00000000, 0b00000000, 0b00000000)
        val wantGameCode = "AAAAA"
        mockCommunicator.bytesIncoming.send(welcomeBytes)

        delay(100L)

        val dataTransmitted = lastDataTransmittedToGarminDevice.captured

        // Symbol unknown (2), field empty, ...
        val wantGameData =
            byteArrayOf(0b10000000.toByte(), 0b00000000, 0b00000000, 0b00010001, 0, 0).toList()

        assertEquals(wantGameData, (dataTransmitted as ByteArray).toList())
    }

    @Test
    fun toggleSymbol() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val garminAppCommunicator = mockk<IQAppCommunicator>()
        val lastDataTransmittedToGarminDevice = slot<Any>()
        every { garminAppCommunicator.transmitData(capture(lastDataTransmittedToGarminDevice)) } just runs

        val onDataReceivedSlot = slot<(Any) -> Unit>()
        every { garminAppCommunicator.setOnAppReceive(capture(onDataReceivedSlot)) } just runs

        GarminGame(garminAppCommunicator, this)


        onDataReceivedSlot.captured(listOf(0)) // Player should be created and websocket connection established.

        delay(1000L)

        // Server sends game code 'AAAAA'
        val welcomeBytes = byteArrayOf(0b00000000, 0b00000000, 0b00000000, 0b00000000)
        mockCommunicator.bytesIncoming.send(welcomeBytes)

        delay(100L)

        onDataReceivedSlot.captured(listOf(1)) // Toggle symbol

        val wantSent = listOf(0b10000000.toByte())
        val haveSent = mockCommunicator.lastBytesSent?.toList()

        assertEquals(wantSent, haveSent)
    }

    @Test
    fun resetGame() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val garminAppCommunicator = mockk<IQAppCommunicator>()
        val lastDataTransmittedToGarminDevice = slot<Any>()
        every { garminAppCommunicator.transmitData(capture(lastDataTransmittedToGarminDevice)) } just runs

        val onDataReceivedSlot = slot<(Any) -> Unit>()
        every { garminAppCommunicator.setOnAppReceive(capture(onDataReceivedSlot)) } just runs

        GarminGame(garminAppCommunicator, this)


        onDataReceivedSlot.captured(listOf(0)) // Player should be created and websocket connection established.

        delay(1000L)

        // Server sends game code 'AAAAA'
        val welcomeBytes = byteArrayOf(0b00000000, 0b00000000, 0b00000000, 0b00000000)
        mockCommunicator.bytesIncoming.send(welcomeBytes)

        delay(100L)

        onDataReceivedSlot.captured(listOf(2)) // Reset game

        val wantSent = listOf(0b01100000.toByte())
        val haveSent = mockCommunicator.lastBytesSent?.toList()

        assertEquals(wantSent, haveSent)
    }

    @Test
    fun gameWithWinner() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val garminAppCommunicator = mockk<IQAppCommunicator>()
        val lastDataTransmittedToGarminDevice = slot<Any>()
        every { garminAppCommunicator.transmitData(capture(lastDataTransmittedToGarminDevice)) } just runs

        val onDataReceivedSlot = slot<(Any) -> Unit>()
        every { garminAppCommunicator.setOnAppReceive(capture(onDataReceivedSlot)) } just runs

        GarminGame(garminAppCommunicator, this)


        onDataReceivedSlot.captured(listOf(0)) // Player should be created and websocket connection established.

        delay(1000L)

        // Server sends game information
        val gameInfoBytes = byteArrayOf(-116, -75, -96)
        mockCommunicator.bytesIncoming.send(gameInfoBytes)

        delay(100L)

        val dataTransmitted = lastDataTransmittedToGarminDevice.captured

        // No game code (normally unrealistic)
        // Symbol o, ...
        val wantGameInfo =
            byteArrayOf(0b01000101, 0b00011111, 0b00000101, 0b10101101.toByte(), 0).toList()
        assertEquals(wantGameInfo, (dataTransmitted as ByteArray).toList())
    }

    @Test
    fun serverSentException() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val garminAppCommunicator = mockk<IQAppCommunicator>()
        val lastDataTransmittedToGarminDevice = slot<String>()
        every { garminAppCommunicator.transmitData(capture(lastDataTransmittedToGarminDevice)) } just runs

        val onDataReceivedSlot = slot<(Any) -> Unit>()
        every { garminAppCommunicator.setOnAppReceive(capture(onDataReceivedSlot)) } just runs

        GarminGame(garminAppCommunicator, this)


        onDataReceivedSlot.captured(listOf(0)) // Player should be created and websocket connection established.

        delay(1000L)

        // Server sends action invalid packet (out of the blue)
        val gameInfoBytes = byteArrayOf(0b10100000.toByte())
        mockCommunicator.bytesIncoming.send(gameInfoBytes)

        delay(100L)

        val dataTransmitted = lastDataTransmittedToGarminDevice.captured

        assertTrue(dataTransmitted.isNotEmpty())
    }

    @Test
    fun newGameTwice() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val garminAppCommunicator = mockk<IQAppCommunicator>()
        every { garminAppCommunicator.transmitData(any()) } just runs

        val onDataReceivedSlot = slot<(Any) -> Unit>()
        every { garminAppCommunicator.setOnAppReceive(capture(onDataReceivedSlot)) } just runs

        GarminGame(garminAppCommunicator, this)


        onDataReceivedSlot.captured(listOf(0)) // Player should be created and websocket connection established.
        assertFalse(mockCommunicator.webSocketConnected)

        delay(1000L)
        assertTrue(mockCommunicator.webSocketConnected)


        delay(4 * 60 * 60 * 1000L) // Wait a long time, the game should be stopped now.

        onDataReceivedSlot.captured(listOf(0)) // A new player should be created and a new websocket connection established.
        assertFalse(mockCommunicator.webSocketConnected)

        delay(1000L)
        assertTrue(mockCommunicator.webSocketConnected)
    }

    @Test
    fun newGameTwice2() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val garminAppCommunicator = mockk<IQAppCommunicator>()
        every { garminAppCommunicator.transmitData(any()) } just runs

        val onDataReceivedSlot = slot<(Any) -> Unit>()
        every { garminAppCommunicator.setOnAppReceive(capture(onDataReceivedSlot)) } just runs

        GarminGame(garminAppCommunicator, this)


        onDataReceivedSlot.captured(listOf(0)) // Player should be created and websocket connection established.
        assertFalse(mockCommunicator.webSocketConnected)

        delay(1000L)
        assertTrue(mockCommunicator.webSocketConnected)


        delay(10 * 60 * 60 * 1000L) // Wait a really long time, the game should definitely be stopped now.

        onDataReceivedSlot.captured(listOf(0)) // A new player should be created and a new websocket connection established.
        assertFalse(mockCommunicator.webSocketConnected)

        delay(1000L)
        assertTrue(mockCommunicator.webSocketConnected)
    }

}