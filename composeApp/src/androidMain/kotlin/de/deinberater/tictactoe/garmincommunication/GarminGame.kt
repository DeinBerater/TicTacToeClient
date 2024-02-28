package de.deinberater.tictactoe.garmincommunication

/** This class manages a game for each device.
 * Created when the device sends its initialize message.
 * */
class GarminGame(private val garminCommunicator: IQAppCommunicator) {

    /** This method is blocking, thus it shall be launched in a coroutine scope.
     * */
    suspend fun listenToGarminDevice() {
        for (garminData in garminCommunicator.appReceiveChannel) {
            println("Data received: $garminData")
        }
    }
}