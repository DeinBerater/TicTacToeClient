package de.deinberater.tictactoe

import App
import Player
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.deinberater.tictactoe.garmincommunication.BackgroundServiceGarmin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val scope = CoroutineScope(Dispatchers.Unconfined)


class MainActivity : ComponentActivity() {
    private val player = Player(scope)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(player)
        }
        startService(Intent(this, BackgroundServiceGarmin::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.launch {
            player.closeConnection()
        }
    }
}