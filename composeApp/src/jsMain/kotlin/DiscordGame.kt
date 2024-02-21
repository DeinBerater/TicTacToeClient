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
        coroutineScope {
            scope = this
            player = Player(this)
            game = player.game()

            val gameCodeEntered =
                originalInteraction.asDynamic().options.getString("code") as String?

            gameCodeEntered?.let {
                player.updateChannel.receive()?.let inner@{
                    sendExceptionMessage(it) // First update has a problem
                    return@let // Return from outer to avoid sending the game code
                }

                try {
                    player.submitGameCode(gameCodeEntered)
                } catch (e: IllegalArgumentException) {
                    (originalInteraction.asDynamic().followUp(
                        "The game code is invalid: ${e.message}",
                        ephemeral = true
                    ) as Promise<Discord.Message>).await()
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
            content = "**Something went wrong:**\n$msg",
            ephemeral = true
        ) as Promise<Discord.Message>
    }

    private suspend fun updateBoard() {
        (originalInteraction.asDynamic()
            .editReply("Sample reply.") as Promise<Discord.Message>)
    }
}