import communication.ByteBuilder
import communication.MockCommunicator
import communication.createCommunicator
import game.Game
import game.TicTacToeSymbol
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerTest {

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

    @Test
    fun playerCreatedNoConnection() = runTest {
        val mockCommunicator = MockCommunicator()
        mockCommunicator.pleaseThrowAnExceptionOnConnection = true
        every { createCommunicator() } returns mockCommunicator

        // Should throw an exception (not immediately)
        val sut = Player(this)

        val update = sut.updateChannel.receive()
        assertNotNull(update) // There is an exception
    }

    @Test
    fun restartConnection() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        sut.game.hasOpponent = true // Manipulate the game
        assertTrue(sut.game.hasOpponent)

        sut.restartConnection()

        // After some time the connection is restarted.
        delay(1000L)

        assertFalse(sut.game.hasOpponent)
    }

    @Test
    fun connectionClosed() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        mockCommunicator.bytesIncoming.send(null)
        val received = sut.updateChannel.receive()
        assertNotNull(received)
    }

    @Test
    fun onOpponentLeave() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        sut.game.hasOpponent = true // Manipulate the game
        assertTrue(sut.game.hasOpponent)

        // Send GameInfo
        mockCommunicator.bytesIncoming.send(ByteBuilder().addInt(3, 3).getBytes())

        assertFalse(sut.game.hasOpponent)

        val update = sut.updateChannel.receive()
        assertNull(update)
    }

    @Test
    fun onPacketInvalid() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        // Send Packet
        mockCommunicator.bytesIncoming.send(ByteBuilder().addInt(1, 3).getBytes())

        // Receive Packet
        val update = sut.updateChannel.receive()

        // Expect exception
        assertNotNull(update)
    }

    @Test
    fun onActionInvalid() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        // Send Packet
        mockCommunicator.bytesIncoming.send(ByteBuilder().addInt(5, 3).getBytes())

        // Receive Packet
        val update = sut.updateChannel.receive()

        // Expect exception
        assertNotNull(update)
    }

    @Test
    fun onGameFull() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        // Send Packet
        mockCommunicator.bytesIncoming.send(ByteBuilder().addInt(7, 3).getBytes())

        // Receive Packet
        val update = sut.updateChannel.receive()

        // Expect exception
        assertNotNull(update)
    }

    @Test
    fun onWelcome() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        // game code 'AAAAA'
        val welcomeBytes = byteArrayOf(0b00000000, 0b00000000, 0b00000000, 0b00000000)
        val want = "AAAAA"

        // Send Packet
        mockCommunicator.bytesIncoming.send(welcomeBytes)

        // Receive Packet
        val update = sut.updateChannel.receive()
        assertNull(update)


        val have = sut.game.gameCode
        assertEquals(want, have)
    }

    @Test
    fun onOpponentMakeMove() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        // Manipulate game
        sut.game.hasOpponent = true
        sut.game.setGameActive(TicTacToeSymbol.X)
        sut.game.onTurn = false

        val bytes = byteArrayOf(0b01001000) // Field no. 4
        val want = TicTacToeSymbol.O

        // Send Packet
        mockCommunicator.bytesIncoming.send(bytes)

        // Receive Packet
        val update = sut.updateChannel.receive()
        assertNull(update)


        val have = sut.game.getSymbolByCoords(1, 1)
        assertEquals(want, have)
    }

    @Test
    fun onGameUpdate() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        val bytes = byteArrayOf(-97, 62, -106)

        // Send Packet
        mockCommunicator.bytesIncoming.send(bytes)

        // Receive Packet
        val update = sut.updateChannel.receive()
        assertNull(update)

        val currentGame = sut.game
        assertEquals(TicTacToeSymbol.O, currentGame.getSymbolByCoords(0, 0))
        assertEquals(TicTacToeSymbol.X, currentGame.getSymbolByCoords(2, 0))
        assertEquals(TicTacToeSymbol.O, currentGame.getSymbolByCoords(2, 1))
        assertEquals(TicTacToeSymbol.X, currentGame.getSymbolByCoords(2, 2))
        assertEquals(TicTacToeSymbol.O, currentGame.getSymbolByCoords(1, 1))
        assertNull(currentGame.getSymbolByCoords(0, 2))

        assertTrue(currentGame.gameActive)
        assertTrue(currentGame.onTurn)
        assertTrue(currentGame.hasOpponent)
        assertEquals(TicTacToeSymbol.X, currentGame.symbol)
    }

    @Test
    fun onGameUpdate2() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        val bytes = byteArrayOf(0b10000001.toByte(), 62, -106)

        // Send Packet
        mockCommunicator.bytesIncoming.send(bytes)

        // Receive Packet
        val update = sut.updateChannel.receive()
        assertNull(update)

        val currentGame = sut.game
        assertEquals(TicTacToeSymbol.O, currentGame.getSymbolByCoords(0, 0))
        assertEquals(TicTacToeSymbol.X, currentGame.getSymbolByCoords(2, 0))
        assertEquals(TicTacToeSymbol.O, currentGame.getSymbolByCoords(2, 1))
        assertEquals(TicTacToeSymbol.X, currentGame.getSymbolByCoords(2, 2))
        assertEquals(TicTacToeSymbol.O, currentGame.getSymbolByCoords(1, 1))
        assertNull(currentGame.getSymbolByCoords(0, 2))

        assertFalse(currentGame.gameActive)
        assertFalse(currentGame.onTurn)
        assertFalse(currentGame.hasOpponent)
        assertEquals(TicTacToeSymbol.O, currentGame.symbol)
    }

    @Test
    fun badServerPacket() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        val bytes = byteArrayOf(0b00000000, 0b01110000, -106)

        // Send Packet
        mockCommunicator.bytesIncoming.send(bytes)

        // Receive Packet
        val update = sut.updateChannel.receive()
        assertNotNull(update) // Exception
    }

    @Test
    fun onGameCodeInvalid() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        delay(1000L) // Delay to make sure the communicator is initialized.

        sut.submitGameCode("ABCDE")

        // Send Packet
        mockCommunicator.bytesIncoming.send(ByteBuilder().addInt(6, 3).getBytes())

        // Receive Packet
        val update = sut.updateChannel.receive()

        // Expect exception
        assertNotNull(update)

        assertNull(sut.game.gameCode)
    }

    @Test
    fun onGameCodeInvalid2() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        // game code 'AAAAA'
        val welcomeBytes = byteArrayOf(0b00000000, 0b00000000, 0b00000000, 0b00000000)
        val want = "AAAAA"

        // Send and receive Packet
        mockCommunicator.bytesIncoming.send(welcomeBytes)
        sut.updateChannel.receive()

        delay(1000L) // Delay to make sure the communicator is initialized.

        sut.submitGameCode("ABCDE")

        // Send Packet
        mockCommunicator.bytesIncoming.send(ByteBuilder().addInt(6, 3).getBytes())

        // Receive Packet
        val update = sut.updateChannel.receive()

        // Expect exception
        assertNotNull(update)

        assertEquals(want, sut.game.gameCode)
    }

    @Test
    fun gameCodeValid() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        // game code 'AAAAA'
        val welcomeBytes = byteArrayOf(0b00000000, 0b00000000, 0b00000000, 0b00000000)
        val newGameCode = "ABCDE"

        // Send and receive Packet
        mockCommunicator.bytesIncoming.send(welcomeBytes)
        sut.updateChannel.receive()

        delay(1000L) // Delay to make sure the communicator is initialized.

        sut.submitGameCode(newGameCode)

        val bytes = byteArrayOf(-97, 62, -106)

        // Send Packet
        mockCommunicator.bytesIncoming.send(bytes)

        // Receive Packet
        val update = sut.updateChannel.receive()
        assertNull(update)

        assertEquals(newGameCode, sut.game.gameCode)
    }

    @Test
    fun submitBadGameCode() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)

        assertFailsWith(IllegalArgumentException::class) {
            sut.submitGameCode("12345")
        }

    }

    @Test
    fun resetBoard() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)
        val want = byteArrayOf(0b01100000).toList()

        delay(1000L) // Delay to make sure the communicator is initialized.

        sut.resetBoard()
        val have = mockCommunicator.lastBytesSent?.toList()

        assertEquals(want, have)
    }

    @Test
    fun toggleSymbol() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)
        val want = byteArrayOf(0b10000000.toByte()).toList()

        delay(1000L) // Delay to make sure the communicator is initialized.

        sut.toggleSymbol()
        val have = mockCommunicator.lastBytesSent?.toList()

        assertEquals(want, have)
    }

    @Test
    fun makeMove() = runTest {
        val mockCommunicator = MockCommunicator()
        every { createCommunicator() } returns mockCommunicator

        // Create player
        val sut = Player(this)
        sut.game.setGameActive(TicTacToeSymbol.X)
        sut.game.hasOpponent = true
        sut.game.onTurn = true

        val wantDataSent = byteArrayOf(0b01001000).toList()

        delay(1000L) // Delay to make sure the communicator is initialized.

        sut.makeMove(1, 1)
        val haveGameUpdate = sut.updateChannel.receive()
        assertNull(haveGameUpdate)

        val haveDataSent = mockCommunicator.lastBytesSent?.toList()
        assertEquals(wantDataSent, haveDataSent)
    }

}
