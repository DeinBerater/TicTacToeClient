package de.deinberater.tictactoe.garmincommunication

import Player
import communication.ByteBuilder
import communication.WebSocketNotConnectedException
import game.FieldCoordinate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** This class manages a game for each device.
 * Created when the device sends its initialize message.
 * */
class GarminGame(
    private val garminCommunicator: IQAppCommunicator,
    private val scope: CoroutineScope
) {
    private var player: Player? = null
    private var gameUpdateListeningJob: Job? = null

    init {
        listenToGarminDevice()
    }

    suspend fun cancelGame() {
        println("Cancelling garmin game...")
        player?.closeConnection()
        gameUpdateListeningJob?.cancel()

        // Reset player to be able to start another game at a later time.
        player = null
    }

    private fun listenToGarminDevice() {
        garminCommunicator.setOnAppReceive { garminData ->
            // The code in here should be non-blocking.
            if (garminData !is List<*>) return@setOnAppReceive
            if (garminData.size != 1) return@setOnAppReceive
            val intSentFromDevice = garminData.first() as? Int ?: return@setOnAppReceive
            // Data is now an integer.

            println("Data received: $garminData")
            if (player == null) {
                player = Player(scope)

                // Start listening job
                gameUpdateListeningJob = scope.launch { listenToGameUpdates() }

                // Start timeout to cancel listening job
                scope.launch {
                    delay(2 * 60 * 60 * 1000L)
                    // Cancel the game automatically after 2 hours to save the phones resources.
                    cancelGame()
                }

                return@setOnAppReceive
            }

            try {
                when (intSentFromDevice) {
                    0 -> transmitCurrentGame()
                    1 -> player?.toggleSymbol()
                    2 -> player?.resetBoard()

                    else -> {
                        val fieldCoordinate = FieldCoordinate(intSentFromDevice - 2)
                        try {
                            player?.makeMove(fieldCoordinate.x, fieldCoordinate.y)
                        } catch (e: Throwable) {
                            if (e is WebSocketNotConnectedException) throw e
                            transmitCurrentGame()
                        }
                    }
                }
            } catch (e: WebSocketNotConnectedException) {
                garminCommunicator.transmitData("There is no connection. Retrying...")

                // Restart the connection to retry
                player?.restartConnection()
            }

        }
    }

    private suspend fun listenToGameUpdates() {
        if (player == null) return
        val updateChannel = player!!.updateChannel
        for (gameUpdate in updateChannel) {
            if (gameUpdate == null) {
                transmitCurrentGame()
                continue
            } else garminCommunicator.transmitData(gameUpdate)
        }
    }

    private fun transmitCurrentGame() {
        println("Transmitting the current game info to a garmin device...")
        val game = player?.game ?: return


        val byteBuilder = ByteBuilder()

        val gameCode = game.gameCode
        if (gameCode != null) {
            byteBuilder.addBoolean(true)
            gameCode.forEach {
                byteBuilder.addInt(it - 'A', 5)
            }
        } else byteBuilder.addBoolean(false)

        val gameWinner = game.winner()
        if (gameWinner != null) {
            byteBuilder.addBoolean(true)
            gameWinner.forEach {
                byteBuilder.addInt(it.toIndex(), 4)
            }
        } else byteBuilder.addBoolean(false)

        byteBuilder.addBoolean(game.onTurn)
        byteBuilder.addBoolean(game.hasOpponent)
        byteBuilder.addBoolean(game.boardFull())
        byteBuilder.addBoolean(game.gameActive)
        byteBuilder.addInt(game.symbol?.ordinal ?: 2, 2)

        for (fieldIndex in 0..8) {
            val fieldCoordinate = FieldCoordinate(fieldIndex)
            val symbolOnField = game.getSymbolByCoords(fieldCoordinate.x, fieldCoordinate.y)
            if (symbolOnField == null) {
                // First boolean indicates if the field has a symbol on it
                byteBuilder.addBoolean(false)
                continue
            }
            // The first boolean indicates if the field has a symbol on it
            // The second boolean indicates which symbol it is
            byteBuilder.addBoolean(true).addInt(symbolOnField.ordinal, 1)
        }

        // Add to transmission queue
        garminCommunicator.transmitData(byteBuilder.getBytes())
    }
}