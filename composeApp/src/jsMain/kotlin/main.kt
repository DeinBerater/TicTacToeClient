/** If an error is thrown here, please read the exception message in lines 6-9.
 * */
val configPrivate = try {
    js("require('../../../../../composeApp/src/jsMain/resources/config_private.json')")
} catch (e: Exception) {
    throw Exception("The private config does not exist! " +
            "Please create a config_private.json file in composeApp/src/jsMain/resources. " +
            "It should contain the key discord_token with a valid discord bot token as value.", e)
}

fun main() {
    console.log("Hello, Kotlin/JS!")
}