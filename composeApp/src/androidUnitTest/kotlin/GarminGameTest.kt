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
import kotlin.test.assertNull
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
        val lastDataTransmittedToGarminDevice = slot<List<Any>>()
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

        assertEquals(wantGameCode, dataTransmitted[0])
        assertNull(dataTransmitted[1]) // No winner

        // Symbol unknown (2), field empty, ...
        val wantGameInfo = byteArrayOf(0b10001000.toByte(), 0b00000000).toList()
        assertEquals(wantGameInfo, (dataTransmitted[2] as ByteArray).toList())
    }

    @Test
    fun toggleSymbol() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        val garminAppCommunicator = mockk<IQAppCommunicator>()
        val lastDataTransmittedToGarminDevice = slot<List<Any>>()
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
        val lastDataTransmittedToGarminDevice = slot<List<Any>>()
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
        val lastDataTransmittedToGarminDevice = slot<List<Any>>()
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

        assertNull(dataTransmitted[0]) // No game code (normally unrealistic)

        val winnerWant = 147
        val winner = dataTransmitted[1]

        assertEquals(winnerWant, winner)

        // Symbol o, ...
        val wantGameInfo =
            byteArrayOf(0b11000001.toByte(), 0b01101011.toByte(), 0b01000000.toByte()).toList()
        assertEquals(wantGameInfo, (dataTransmitted[2] as ByteArray).toList())
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
}