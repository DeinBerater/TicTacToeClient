import communication.doAsynchronously
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

/** If an error is thrown here, please read the exception message in lines 6-9.
 * */
val configPrivate = try {
    js("require('../../../../../composeApp/src/jsMain/resources/config_private.json')")
} catch (e: Exception) {
    throw Exception(
        "The private config does not exist! " +
                "Please create a config_private.json file in composeApp/src/jsMain/resources. " +
                "It should contain the key discord_token with a valid discord bot token as value.",
        e
    )
}

fun main() {

    val client = Discord.Client(js("{ intents: [] }"))

    client.login(configPrivate.discord_token as String)

    client.on("ready") {
        console.log("Logged in as ${client.user.tag}!")
    }

    client.on("interactionCreate") { interaction ->
        interaction as Discord.BaseInteraction

        if (!interaction.asDynamic().isCommand() as Boolean) return@on
        // Interaction is a command interaction

        // Now everything in the game is handled in this class
        val game = DiscordGame(interaction as Discord.CommandInteraction)

        doAsynchronously {
            game.startGame()
        }
    }
}

// Enable promises to be awaited
suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}

inline fun jsObject(init: dynamic.() -> Unit): dynamic {
    val o = js("{}")
    init(o)
    return o
}