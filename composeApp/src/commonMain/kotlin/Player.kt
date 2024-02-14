import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class Player(
    private val game: Game,
    private val scope: CoroutineScope
) {
    val updateChannel = Channel<Unit>()

    suspend fun makeWebRequest() {
        game.extraInfo = getWebRequestInstance()?.getWebsite() ?: "nah"
        updateUi()
    }

    private fun updateUi() {
        scope.launch {
            updateChannel.send(Unit)
        }
    }
}