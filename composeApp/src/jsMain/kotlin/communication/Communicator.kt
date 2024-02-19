package communication

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual fun getCommunicatorEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = Js

@OptIn(DelicateCoroutinesApi::class)
actual fun doAsynchronously(block: suspend () -> Unit) {
    GlobalScope.promise {
        block()
    }
}