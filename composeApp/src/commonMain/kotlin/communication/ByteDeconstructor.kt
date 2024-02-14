package communication

/** A class which reads different types of data from a [ByteArray]
 * */
class ByteDeconstructor(private val byteArray: ByteArray) {
    private var currentBitPosition = 0
    private var currentBytePosition = 0

    /** Reads a boolean value.
     * */
    fun readBoolean(): Boolean {
        val bitRead = getCurrentBitToRead()
        advanceOneBit()

        return bitRead == 1
    }

    /** Reads an integer value.
     * @param bitLength the length of the bits the int should have
     * */
    fun readInt(bitLength: Int): Int {
        if (bitLength < 1) throw IllegalArgumentException("Wrong bit length!")
        var currentInt = 0
        for (i in 0..<bitLength) {
            val currentBit = getCurrentBitToRead() // get current bit
            currentInt = currentInt shl 1 // shifts currentInt left
            currentInt = currentInt or currentBit // add current bit to the right
            advanceOneBit()
        }
        return currentInt
    }

    /** Advances one bit to be able to read the next. If the current byte is over, it reads the next byte and
     *  resets the bit position.
     * */
    private fun advanceOneBit() {
        if (currentBitPosition < 7) {
            currentBitPosition++
            return
        } else {
            // position = 7

            // Get next byte, even if there is none
            currentBytePosition++

            // Go back 8 bits
            currentBitPosition = 0
        }
    }

    /** @return if the current byte does not exist.
     * */
    private fun empty() = (currentBytePosition == byteArray.size)

    /** Removes all data from this stream and checks if there is any data left. Call this when you extracted all data
     *  a packet should have.
     * @throws InvalidPacketException if there is any data left.
     * */
    @Throws(InvalidPacketException::class)
    fun finish() {
        if (empty()) return

        val restBitsInByte =
            (byteArray[currentBytePosition].toInt() shl currentBitPosition) and 0b11111111
        if (currentBytePosition == byteArray.size - 1 && restBitsInByte == 0) return

        throw InvalidPacketException("Packet contains too much information!")
    }

    /** Gets the current bit which should be read.
     * */
    private fun getCurrentBitToRead(): Int {
        if (empty()) throw InvalidPacketException("Packet too short!")
        return (byteArray[currentBytePosition].toInt() shr (7 - currentBitPosition)) and 1
    }
}
