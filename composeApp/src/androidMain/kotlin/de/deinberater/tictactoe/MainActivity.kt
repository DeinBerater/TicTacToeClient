package de.deinberater.tictactoe

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
//            val height = window.decorView.height
//            val width = window.decorView.width
//            val smaller = min(height, width)
//            val modifier = Modifier.size(smaller.dp, smaller.dp)
//            println("$height sdafasd $width")
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}