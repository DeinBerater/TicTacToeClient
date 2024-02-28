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

private val scope = CoroutineScope(Dispatchers.Unconfined)


class MainActivity : ComponentActivity() {
    private val player = Player(CoroutineScope(Dispatchers.Default))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(player)
        }
        startService(Intent(this, BackgroundServiceGarmin::class.java))
    }
}