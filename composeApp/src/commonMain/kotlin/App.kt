import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun App() {

    val scope = rememberCoroutineScope()

    val player = Player(scope)
    val game = player.game()

    MaterialTheme {

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally)
        {
            var timesUpdated by remember { mutableStateOf("") }

            Text("On turn: ${game.onTurn}")
            Text("Symbol: ${game.symbol}")
            Text("Opponent connected: ${game.hasOpponent}")
            Text("Game Code: ${game.gameCode}")
            Text("First: " + game.getSymbolByCoords(0, 0))

            Text(timesUpdated) // This is needed to update the ui on game changes..

            scope.launch {
                // Wait for the channel to demand an UI update
                val exceptionMessage = player.updateChannel.receive()

                if (exceptionMessage != null) {
                    // ToDo: Good exception handling here
                    println("An exception occurred:")
                    println(exceptionMessage)
                    return@launch
                }

                // Change anything to update the UI.. Little trick (we do not talk about that.)
                timesUpdated = if (timesUpdated == "") " " else ""
            }

        }
    }
}