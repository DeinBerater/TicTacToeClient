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

    private var connectionClosed = false

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
                    return@coroutineScope // Return from outer to avoid sending the game code
                }

                try {
                    player.submitGameCode(gameCodeEntered)
                } catch (e: Exception) {
                    sendExceptionMessage(e.message ?: "The game code could not be sent.")
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

    private suspend fun sendExceptionMessage(msg: String) {
        if (!originalInteraction.asDynamic().replied as Boolean) {
            player.closeConnection()
            connectionClosed = true
        }

        val embed = Discord.EmbedBuilder()
            .setColor("#FF0000")
            .setTitle("Something went wrong")
            .setDescription(msg)

        originalInteraction.asDynamic().followUp(
            jsObject {
                embeds = arrayOf(embed)
            }
        ) as Promise<Discord.Message>
    }

    private fun updateBoard() {

        var actionRows = arrayOf<Discord.ActionRowBuilder>()
        for (rowCount in 0..2) {
            val row = Discord.ActionRowBuilder()
            for (column in 0..2) {
                var buttonBuilder = Discord.ButtonBuilder()
                    .setCustomId("" + rowCount + column)
                    .setStyle(Discord.ButtonStyle.Secondary)
                    .setDisabled(!game.gameActive || !game.onTurn)

                val symbolOnField = game.getSymbolByCoords(column, rowCount)

                buttonBuilder = if (symbolOnField == null) {
                    // Zero-width spaces for all buttons to have similar sizes
                    buttonBuilder.setLabel("\u200B \u200B")
                } else {
                    buttonBuilder.setEmoji(symbolOnField.asEmoji())
                        .setStyle(Discord.ButtonStyle.Primary)
                }

                row.addComponents(buttonBuilder)
            }
            actionRows += row
        }

        val resetButton = Discord.ButtonBuilder()
            .setStyle(Discord.ButtonStyle.Primary)
            .setLabel("Reset game")
            .setCustomId("reset")
            .setDisabled(connectionClosed)

        val toggleSymbolButton = Discord.ButtonBuilder()
            .setStyle(Discord.ButtonStyle.Primary)
            .setLabel("Toggle symbol")
            .setCustomId("toggle")
            .setDisabled(connectionClosed)

        val disconnectButton = Discord.ButtonBuilder()
            .setStyle(Discord.ButtonStyle.Danger)
            .setLabel("Close connection")
            .setCustomId("disconnect")
            .setDisabled(connectionClosed)

        actionRows += Discord.ActionRowBuilder().addComponents(resetButton, toggleSymbolButton)
        actionRows += Discord.ActionRowBuilder().addComponents(disconnectButton)

        val emojiOnTurn =
            if (game.onTurn) game.symbol?.asEmoji() else game.symbol?.other()?.asEmoji()

        var messageContent = "Game code: **${game.gameCode ?: "unknown"}**"
        messageContent += "\n**" + (if (!game.hasOpponent) "Waiting for opponent..." else
            ("$emojiOnTurn " + (if (game.onTurn) "You are" else "Your opponent is") + " on turn! $emojiOnTurn")) + "**"

        (originalInteraction.asDynamic()
            .editReply(jsObject {
                content = messageContent
                components = actionRows
            }) as Promise<Discord.Message>)
    }
}