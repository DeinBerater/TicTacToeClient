package communication

class Communicator : BaseCommunicator() {
    override suspend fun connectWithWebsocket() {
        TODO("Not yet implemented")
    }

    override suspend fun sendBytes(bytes: ByteArray) {
        TODO("Not yet implemented")
    }

}

actual fun createCommunicator(): BaseCommunicator = Communicator()