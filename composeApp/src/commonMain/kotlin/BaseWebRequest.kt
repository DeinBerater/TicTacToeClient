interface BaseWebRequest {
    val client: Any // Temp

    suspend fun getWebsite(): String
}

expect fun getWebRequestInstance(): BaseWebRequest?