package de.deinberater.nigglgarminmobile.devicecommunication

enum class QueueState {
    /**The queue is not transmitting data, even if it's not empty*/
    IDLE,

    /**The queue is not transmitting data, because it's empty*/
    READY,

    /**The queue is currently transmitting data.*/
    WORKING,
}