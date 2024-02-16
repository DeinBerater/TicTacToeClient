import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import communication.WebSocketNotConnectedException
import game.TicTacToeSymbol
import kotlinx.coroutines.launch

@Composable
fun App(darkTheme: Boolean = isSystemInDarkTheme()) {

    val scope = rememberCoroutineScope()

    val player = Player(scope)
    val game = player.game()

    // ToDo: Implement dark theme
    MaterialTheme {

        val openAlertDialog = remember { mutableStateOf(false) }
        val lastExceptionMessage = remember { mutableStateOf(null as String?) }

        when {
            openAlertDialog.value -> {
                if (lastExceptionMessage.value!!.startsWith("Error in WebSocket connection")) {
                    alertDialog(
                        "Websocket connection failed",
                        lastExceptionMessage.value!!,
                        "Ok", {
                            openAlertDialog.value = false
                        },
                        "Retry", {
                            openAlertDialog.value = false
                            player.restartConnection()
                        }
                    )
                } else {
                    alertDialog(
                        "Something went wrong",
                        lastExceptionMessage.value!!,
                        "Ok", {
                            openAlertDialog.value = false
                        }
                    )
                }
            }
        }


        // Ran if there is any exception which should be shown in a popup.
        fun onException(message: String) {
            lastExceptionMessage.value = message
            openAlertDialog.value = true
        }


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            var timesUpdated by remember { mutableStateOf("") }

            Text(timesUpdated) // This is needed to update the ui on game changes..

            scope.launch {
                // Wait for the channel to demand an UI update
                val exceptionMessage = player.updateChannel.receive()

                if (exceptionMessage != null) {
                    onException(exceptionMessage)
                    return@launch
                }

                // Change anything to update the UI.. Little trick (we do not talk about that.)
                timesUpdated = if (timesUpdated == "") " " else ""
            }


            val displayedGameCode = game.gameCode ?: "-"

            Text(
                buildAnnotatedString {
                    append("Your Game Code: ")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(displayedGameCode)
                    }

                }, fontSize = 25.sp
            )

            var gameCodeEntered by remember { mutableStateOf("") }
            fun onCodeSubmit() {
                try {
                    player.submitGameCode(gameCodeEntered)
                } catch (e: Exception) {
                    onException(e.message ?: e::class.simpleName ?: "Unknown error.")
                }
            }
            TextField(
                modifier = Modifier.padding(10.dp).onKeyEvent {
                    if (it.key == Key.Enter || it.key == Key.NumPadEnter) {
                        onCodeSubmit()
                        true
                    } else false
                },
                value = gameCodeEntered,
                onValueChange = {
                    gameCodeEntered = it
                },
                label = { Text("Enter game code here") },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    focusedLabelColor = Color.Black,
                    focusedIndicatorColor = Color.Blue,
                    cursorColor = Color.Blue
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onCodeSubmit() }
                )
            )

            Text(
                if (!game.hasOpponent) "No opponent connected." else if (game.onTurn) "It's your turn!" else "Waiting for opponent to move...",
                modifier = Modifier.padding(5.dp),
                fontSize = 20.sp,
                color = if (!game.hasOpponent) Color.Red else if (game.onTurn) Color.Green else Color.Blue,
            )
            Text(
                "You are: ${game.symbol ?: "-"}",
                modifier = Modifier.paddingFromBaseline(top = 5.dp),
                fontSize = 20.sp
            )


            Canvas(
                modifier = Modifier.fillMaxHeight(0.8F).aspectRatio(1F)
                    .padding(20.dp)
                    .pointerInput(true) {
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

            Row(
                modifier = Modifier.padding(5.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.Center
            ) {
                val resetButtonContent by remember { mutableStateOf("Reset Game") }
                val toggleSymbolButtonContent by remember { mutableStateOf("Toggle Symbol") }

                Button(
                    modifier = Modifier.padding(4.dp).weight(0.5f).fillMaxHeight(0.8F),
                    onClick = {
                        println("Trying to reset board...")
                        try {
                            player.resetBoard()
                        } catch (e: WebSocketNotConnectedException) {
                            onException("There is no connection.")
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.LightGray)
                ) {
                    Text(resetButtonContent, color = Color.Red)
                }

                Button(
                    modifier = Modifier.padding(4.dp).weight(0.5f).fillMaxHeight(0.8F),
                    onClick = {
                        println("Trying to toggle symbol...")
                        try {
                            player.toggleSymbol()
                        } catch (e: WebSocketNotConnectedException) {
                            onException("There is no connection.")
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.LightGray)
                ) {
                    Text(toggleSymbolButtonContent, color = Color.Blue)
                }
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
            start = Offset(
                x = boxCenter.x - symbolSize / 2F,
                y = boxCenter.y - symbolSize / 2F
            ),
            end = Offset(x = boxCenter.x + symbolSize / 2F, y = boxCenter.y + symbolSize / 2F),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = colorX,
            start = Offset(
                x = boxCenter.x - symbolSize / 2F,
                y = boxCenter.y + symbolSize / 2F
            ),
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