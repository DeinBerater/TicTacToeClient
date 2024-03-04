import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun main() = application {
    val icon = painterResource("icon_linux.png")

    Window(onCloseRequest = ::exitApplication, title = "TicTacToe", icon = icon) {
        App(Player(CoroutineScope(Dispatchers.Default)))
    }
}