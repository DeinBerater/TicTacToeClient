import game.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.js.Promise

/** A TicTacToe game in discord
 **/
class DiscordGame(private val originalInteraction: Discord.CommandInteraction) {
    private lateinit var scope: CoroutineScope
    private lateinit var player: Player
    private lateinit var game: Game

    suspend fun startGame() {
        (originalInteraction.asDynamic().deferReply() as Promise<Discord.Message>).await()

        val gameCodeEntered =
            (originalInteraction.asDynamic().options.getString("code") as String?)?.uppercase()

        // If the code entered is invalid, the client does not attempt to make a connection to the server.
        gameCodeEntered?.let {
            try {
                Player.validateGameCode(gameCodeEntered)
            } catch (e: IllegalArgumentException) {
                sendExceptionMessage("The game code is invalid: ${e.message}")
                return // Return to avoid a game here.
            }
        }

        coroutineScope {
            // The game is trying to be initialized here.
            scope = this
            player = Player(this)
            game = player.game()


            gameCodeEntered?.let {
                // Receive the first update to simply ignore it, because a new game code is submitted.
                player.updateChannel.receive()?.let inner@{
                    sendExceptionMessage(it) // First update (welcome) has a problem
                    return@let // Return from outer to avoid sending the game code
                }

                try {
                    player.submitGameCode(gameCodeEntered)
                } catch (e: Exception) {
                    sendExceptionMessage(e.message ?: "The game code could not be sent.")
                    player.closeConnection()
                    return@coroutineScope
                }
            }

            listenToGameChanges()
        }
    }

    private suspend fun listenToGameChanges() {
        scope.launch {
            for (update in player.updateChannel) {
                if (update == null) {
                    updateBoard()
                } else {
                    sendExceptionMessage(update)
                }
            }
        }
    }

    private fun sendExceptionMessage(msg: String) {
        originalInteraction.asDynamic().followUp( // ToDo: Discord Embed
            jsObject {
                content = "**Something went wrong:**\n$msg"
            }
        ) as Promise<Discord.Message>
    }

    private fun updateBoard() {
        (originalInteraction.asDynamic()
            .editReply("Sample reply.") as Promise<Discord.Message>)
    }
}