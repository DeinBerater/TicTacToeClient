package communication

import kotlin.experimental.or

/** A class which converts arguments into bits and gives a [ByteArray] of these bits as bytes.
 * */
class ByteBuilder {
    private var byteArray = byteArrayOf()
    private var currentByte: Byte = 0
    private var currentBitPosition =
        -1 // Begin at -1, because the counter is incremented before writing
    // shift 7 - currentByte

    private fun advanceOneBit() {
        if (currentBitPosition < 7) {
            currentBitPosition++
            return
        } else {
            // position = 7

            byteArray += currentByte

            // Create new byte
            currentByte = 0

            // Reset position
            currentBitPosition = 0
        }

    }

    /** Adds a boolean value to the current bits. true = 1; false = 0
     * @param bool the boolean to add
     * @return itself
     * */
    fun addBoolean(bool: Boolean) = addBit(if (bool) 1 else 0)

    /** Adds an integer value to the current bits.
     * @param int the integer value
     * @param bitSize how many bits the integer takes
     * @throws IllegalArgumentException if the integer value takes more bits than [bitSize]
     * @return itself
     * */
    fun addInt(int: Int, bitSize: Int): ByteBuilder {
        if ((int shr bitSize) != 0) throw IllegalArgumentException("Integer value too big!")

        for (i in bitSize - 1 downTo 0) {
            // Shift i to the right and extract most right bit
            addBit((int shr i) and 1)
        }
        return this
    }

    /** Adds a bit to the current bits
     * @param bit the bit to add, obviously either 1 or 0
     * @return itself
     * */
    private fun addBit(bit: Int): ByteBuilder {
        // Advance before adding in case a new byte has to be created.
        advanceOneBit()

        // Shift left boolean value and or it with the current byte.
        currentByte = (((bit shl (7 - currentBitPosition)).toByte()) or currentByte)
        return this
    }

    /** @return A byte array of everything built in this instance.
     * */
    fun getBytes(): ByteArray {
        return if (currentBitPosition >= 0) byteArray.plus(currentByte)
        else byteArray
    }
}