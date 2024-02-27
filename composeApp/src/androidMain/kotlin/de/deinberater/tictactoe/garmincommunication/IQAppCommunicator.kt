package de.deinberater.tictactoe.garmincommunication

import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import kotlinx.coroutines.channels.Channel

class IQAppCommunicator(
    val appReceiveChannel: Channel<MutableList<Any>>, private val transmittingInstance: ConnectIQ,
    private val iqApp: IQApp,
    private val device: IQDevice
) {
    // ToDo
    
    fun getDeviceId() = device.deviceIdentifier

    fun stopQueue() {
        TODO()
    }

    fun continueQueue() {
        TODO()
    }
}