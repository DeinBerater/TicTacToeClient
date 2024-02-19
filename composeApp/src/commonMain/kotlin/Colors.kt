import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

object Colors {

    private val blue = Color.Blue
    private val lighterBlue = Color(0xFF3498db)
    private val lightBlue = Color(0xFF0984e3)
    private val red = Color.Red

    private val lightGray = Color.LightGray
    private val darkGray = Color.DarkGray

    val darkColors = darkColors(
        primary = lighterBlue,
        primaryVariant = lightBlue,
        secondary = darkGray,
        secondaryVariant = red
    )
    val lightColors = lightColors(
        primary = blue,
        primaryVariant = blue,
        secondary = lightGray,
        secondaryVariant = red
    )

}