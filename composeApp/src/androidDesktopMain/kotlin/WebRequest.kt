import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class WebRequest : BaseWebRequest {
    override val client = HttpClient(CIO)

    override suspend fun getWebsite(): String {
        val response = client.get("https://ktor.io/")
        return response.bodyAsText()
    }
}

actual fun getWebRequestInstance(): BaseWebRequest? = WebRequest()