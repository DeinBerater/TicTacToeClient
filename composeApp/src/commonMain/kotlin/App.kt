import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import game.TicTacToeSymbol
import kotlinx.coroutines.launch

@Composable
fun App(modifier: Modifier = Modifier.fillMaxSize(), darkTheme: Boolean = isSystemInDarkTheme()) {
    // The platform can decide for itself which modifier the canvas should have

    val scope = rememberCoroutineScope()

    val player = Player(scope)
    val game = player.game()

    Canvas(modifier = modifier.pointerInput(true) {
        detectTapGestures {
            val boxWidth = size.width / 3
            val boxHeight = size.height / 3

            val x = it.x.toInt() / boxWidth
            val y = it.y.toInt() / boxHeight

            println("Clicked on field ($x, $y).")
            try {
                // Also updates ui.
                player.makeMove(x, y)
            } catch (e: Exception) {
                println("Exception when making a move: ${e::class.simpleName}")
                // In case of an invalid move
            }
        }
    }) {
        drawField()
        for (y in 0..2) {
            for (x in 0..2) {
                val symbol = game.getSymbolByCoords(x, y)
                symbol?.let { drawSymbol(it, x, y) }
            }
        }
    }


    // ToDo: Implement dark theme
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

private fun DrawScope.drawField() {
    // Vertical lines
    for (i in 1..2) {
        val linePosX = size.width * (i / 3f)
        // size: Canvas size
        drawLine(
            color = Color.Black,
            start = Offset(
                x = linePosX,
                y = 0f
            ),
            end = Offset(
                x = linePosX,
                y = size.height
            ),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Horizontal lines
    for (i in 1..2) {
        val linePosY = size.height * (i / 3f)

        // size: Canvas size
        drawLine(
            color = Color.Black,
            start = Offset(
                x = 0f,
                y = linePosY,
            ),
            end = Offset(
                x = size.width,
                y = linePosY,
            ),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

}

private fun DrawScope.drawSymbol(symbol: TicTacToeSymbol, fieldX: Int, fieldY: Int) {
    val colorX = Color.Red
    val colorO = Color.Blue

    val boxWidth = size.width / 3
    val boxHeight = size.height / 3

    val boxCenter = Offset(
        x = center.x + (fieldX - 1) * boxWidth,
        y = center.y + (fieldY - 1) * boxHeight
    )

    val symbolSize = boxWidth * (3 / 4F)

    if (symbol == TicTacToeSymbol.X) {
        drawLine(
            color = colorX,
            start = Offset(x = boxCenter.x - symbolSize / 2F, y = boxCenter.y - symbolSize / 2F),
            end = Offset(x = boxCenter.x + symbolSize / 2F, y = boxCenter.y + symbolSize / 2F),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = colorX,
            start = Offset(x = boxCenter.x - symbolSize / 2F, y = boxCenter.y + symbolSize / 2F),
            end = Offset(x = boxCenter.x + symbolSize / 2F, y = boxCenter.y - symbolSize / 2F),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        return
    }
    // Symbol is circle
    drawCircle(
        color = colorO,
        center = boxCenter,
        radius = symbolSize / 2F,
        style = Stroke(
            width = 4.dp.toPx()
        )
    )
}