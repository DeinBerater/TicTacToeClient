@JsModule("discord.js")
@JsNonModule
external object Discord {
    class Client(js: dynamic) {
        fun login(token: String)
        fun on(s: String, function: (Any?) -> Unit)

        val user: dynamic
    }

    open class BaseInteraction
    class CommandInteraction : BaseInteraction

    class Message
}