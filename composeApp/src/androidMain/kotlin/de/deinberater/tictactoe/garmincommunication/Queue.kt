package de.deinberater.nigglgarminmobile.devicecommunication

open class Queue<T> {
    private val list = arrayListOf<T>()

    private fun isEmpty() = (list.size == 0)
    fun getSize() = list.size
    fun getFirst() = list.firstOrNull()
    open fun add(data: T, ignoreFirst: Boolean = false) {
        list.add(data)
    }

    fun next(): T? {
        if (isEmpty()) return null

        list.removeAt(0)
        return getFirst()
    }
}