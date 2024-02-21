@JsModule("discord.js")
@JsNonModule
external object Discord {
    class Client(js: dynamic) {
        fun on(s: String, function: (Any?) -> Unit)
    }

    open class BaseInteraction
    class CommandInteraction : BaseInteraction

    class Message

    class EmbedBuilder {
        fun setColor(color: Any?): EmbedBuilder
        fun setTitle(title: String?): EmbedBuilder
        fun setDescription(description: String?): EmbedBuilder
    }

    class ActionRowBuilder {
        fun addComponents(vararg components: Any): ActionRowBuilder
    }

    class ButtonBuilder {
        fun setCustomId(customId: String): ButtonBuilder
        fun setDisabled(disabled: Boolean): ButtonBuilder
        fun setEmoji(emoji: Any): ButtonBuilder
        fun setLabel(label: String): ButtonBuilder
        fun setStyle(style: ButtonStyle): ButtonBuilder
        fun setURL(url: String): ButtonBuilder
    }

    object ButtonStyle {
        val Danger: ButtonStyle
        val Link: ButtonStyle
        val Primary: ButtonStyle
        val Secondary: ButtonStyle
        val Success: ButtonStyle
    }
}