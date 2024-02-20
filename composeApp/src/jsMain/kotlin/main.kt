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


@JsModule("discord.js")
@JsNonModule
external object Discord {
    class Client(js: dynamic) {
        fun login(token: String)
        fun on(s: String, function: () -> Unit)

        val user: dynamic
    }
}

fun main() {

    console.log("nodejs started!")

    val client = Discord.Client(js("{ intents: [] }"))

    client.login(configPrivate.discord_token as String);

    client.on("ready") {
        console.log("Logged in as ${client.user.tag}!")
    }
}