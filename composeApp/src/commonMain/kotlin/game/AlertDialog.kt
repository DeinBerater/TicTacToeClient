import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun alertDialog(
    dialogTitle: String,
    dialogText: String,
    mainButtonName: String,
    onMainButtonPress: () -> Unit,
    button2Name: String? = null,
    onButton2Press: (() -> Unit)? = null,
) {
    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            if (onButton2Press != null) {
                onButton2Press()
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onMainButtonPress()
                }
            ) {
                Text(mainButtonName)
            }
        },
        dismissButton = {
            if (button2Name != null) {
                TextButton(
                    onClick = {
                        if (onButton2Press != null) {
                            onButton2Press()
                        }
                    }
                ) {
                    Text(button2Name)
                }
            }
        }
    )
}