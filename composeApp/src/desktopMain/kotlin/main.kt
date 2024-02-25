import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "TicTacToe") {
        App(Player(CoroutineScope(Dispatchers.Default)))
    }
}