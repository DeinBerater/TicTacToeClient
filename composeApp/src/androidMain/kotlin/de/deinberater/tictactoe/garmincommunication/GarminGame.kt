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
    private lateinit var player: Player
    private var gameUpdateListeningJob: Job? = null

    init {
        listenToGarminDevice()

        scope.launch {
            delay(3 * 60 * 60 * 1000L)
            // Cancel the game automatically after 3 hours to save the phones resources.
            cancelGame()
        }
    }

    suspend fun cancelGame() {
        player.closeConnection()
        gameUpdateListeningJob?.cancel()
    }

    private fun listenToGarminDevice() {
        garminCommunicator.setOnAppReceive { garminData ->
            // The code in here should be non-blocking.
            if (garminData !is List<*>) return@setOnAppReceive
            if (garminData.size != 1) return@setOnAppReceive
            val intSentFromDevice = garminData.first() as? Int ?: return@setOnAppReceive
            // Data is now an integer.

            println("Data received: $garminData")
            if (!this@GarminGame::player.isInitialized) {
                player = Player(scope)
                gameUpdateListeningJob = scope.launch { listenToGameUpdates() }
                return@setOnAppReceive
            }

            when (intSentFromDevice) {
                0 -> transmitCurrentGame()
                1 -> try {
                    player.toggleSymbol()
                } catch (e: WebSocketNotConnectedException) {
                    garminCommunicator.transmitData("There is no connection.")
                }

                2 -> try {
                    player.resetBoard()
                } catch (e: WebSocketNotConnectedException) {
                    garminCommunicator.transmitData("There is no connection.")
                }

                else -> {
                    val fieldCoordinate = FieldCoordinate(intSentFromDevice - 2)
                    try {
                        player.makeMove(fieldCoordinate.x, fieldCoordinate.y)
                    } catch (e: WebSocketNotConnectedException) {
                        garminCommunicator.transmitData("There is no connection.")
                    } catch (e: Exception) {
                        transmitCurrentGame()
                    }
                }
            }
        }
    }

    private suspend fun listenToGameUpdates() {
        for (gameUpdate in player.updateChannel) {
            if (gameUpdate == null) {
                transmitCurrentGame()
                return
            } else garminCommunicator.transmitData(gameUpdate)
        }
    }

    private fun transmitCurrentGame() {
        println("Transmitting the current game info to a garmin device...")
        val game = player.game

        val dataToSend = mutableListOf<Any?>()

        dataToSend += game.gameCode

        val gameWinner = game.winner()
        if (gameWinner != null) {
            var currentNum = 0
            gameWinner.forEach {
                currentNum *= 10
                currentNum += it.toIndex()
            }
            dataToSend += currentNum
        } else dataToSend += null

        val byteBuilder = ByteBuilder()
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

        dataToSend += byteBuilder.getBytes()


        // Add tp transmission queue
        garminCommunicator.transmitData(dataToSend)
    }
}