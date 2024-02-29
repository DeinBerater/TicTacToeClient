package de.deinberater.tictactoe.garmincommunication

import Player
import communication.ByteBuilder
import communication.WebSocketNotConnectedException
import game.FieldCoordinate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** This class manages a game for each device.
 * Created when the device sends its initialize message.
 * */
class GarminGame(
    private val garminCommunicator: IQAppCommunicator,
    private val scope: CoroutineScope
) {
    private lateinit var player: Player

    /** This method is blocking, thus it shall be launched in a coroutine scope.
     * */
    suspend fun listenToGarminDevice() {
        for (garminData in garminCommunicator.appReceiveChannel) {
            println(garminData.toString())
            // The code in here should be non-blocking.
            if (garminData !is List<*>) continue
            if (garminData.size != 1) continue
            val intSentFromDevice = garminData.first() as? Int ?: continue
            // Data is now an integer.

            println("Data received: $garminData")
            if (!this@GarminGame::player.isInitialized) {
                player = Player(scope)
                scope.launch { listenToGameUpdates() }
                continue
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
                currentNum += it.toIndex()
                currentNum *= 10
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