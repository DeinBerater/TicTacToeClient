package communication

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ByteDeconstructorTest {

    @Test
    fun readBoolean() {
        val byteArray = byteArrayOf(0b00000000, 0b00000000)
        val sut = ByteDeconstructor(byteArray)

        val have = sut.readBoolean()

        assertFalse(have)
    }

    @Test
    fun readBoolean2() {
        val byteArray = byteArrayOf(0b00000000, 0b00000001)
        val sut = ByteDeconstructor(byteArray)

        val have = sut.readBoolean()

        assertFalse(have)
    }

    @Test
    fun readBoolean3() {
        val byteArray = byteArrayOf(-128, 0b00000000)
        val sut = ByteDeconstructor(byteArray)

        val have = sut.readBoolean()

        assertTrue(have)
    }


    @Test
    fun readBoolean4() {
        val byteArray = byteArrayOf(-1, -1)
        val sut = ByteDeconstructor(byteArray)

        val have = sut.readBoolean()

        assertTrue(have)
    }

    @Test
    fun readBoolean5() {
        val byteArray = byteArrayOf(127, -1)
        val sut = ByteDeconstructor(byteArray)

        val have = sut.readBoolean()

        assertFalse(have)
    }

    @Test
    fun readInt() {
        val byteArray = byteArrayOf(0b00000000, 0b00000000)
        val sut = ByteDeconstructor(byteArray)
        val want = 0

        val have = sut.readInt(3)

        assertEquals(want, have)
    }


    @Test
    fun readInt2() {
        val byteArray = byteArrayOf(0b00000000, 0b00000000)
        val sut = ByteDeconstructor(byteArray)
        val want = 0

        val have = sut.readInt(13)

        assertEquals(want, have)
    }

    @Test
    fun readInt3() {
        val byteArray = byteArrayOf(0b00000011, 0b00000000)
        val sut = ByteDeconstructor(byteArray)
        val want = 3

        val have = sut.readInt(8)

        assertEquals(want, have)
    }


    @Test
    fun readInt4() {
        val byteArray = byteArrayOf(0b00000010, 0b01000000)
        val sut = ByteDeconstructor(byteArray)
        val want = 9

        val have = sut.readInt(10)

        assertEquals(want, have)
    }

    @Test
    fun readInt5() {
        val byteArray = byteArrayOf(0b00000010, 0b01000011)
        val sut = ByteDeconstructor(byteArray)
        val want = 579

        val have = sut.readInt(16)

        assertEquals(want, have)
    }

    @Test
    fun readCombination() {
        val byteArray = byteArrayOf(0b01011110, 0b01000011)
        val sut = ByteDeconstructor(byteArray)

        val expectedBool1 = false
        val expectedInt1 = 2
        val expectedInt2 = 7
        val expectedBool2 = true
        val expectedInt3 = 8

        val bool1 = sut.readBoolean()
        val int1 = sut.readInt(2)
        val int2 = sut.readInt(3)
        val bool2 = sut.readBoolean()
        val int3 = sut.readInt(6)

        assertEquals(expectedBool1, bool1)
        assertEquals(expectedInt1, int1)
        assertEquals(expectedInt2, int2)
        assertEquals(expectedBool2, bool2)
        assertEquals(expectedInt3, int3)
    }

    @Test
    fun readIntTooLong() {
        val byteArray = byteArrayOf(0b00000010, 0b01000000)
        val sut = ByteDeconstructor(byteArray)

        assertFailsWith(InvalidPacketException::class) {
            sut.readInt(17)
        }
    }

    @Test
    fun readIntTooShort() {
        val byteArray = byteArrayOf(0b00000010, 0b01000000)
        val sut = ByteDeconstructor(byteArray)

        assertFailsWith(IllegalArgumentException::class) {
            sut.readInt(0)
        }
    }

    @Test
    fun finishAllEmpty() {
        val byteArray = byteArrayOf(0b00000010, 0b01000000)
        val sut = ByteDeconstructor(byteArray)
        sut.readInt(10)

        sut.finish()
    }

    @Test
    fun finishNoneLeft() {
        val byteArray = byteArrayOf(0b00000010)
        val sut = ByteDeconstructor(byteArray)
        sut.readInt(8)

        sut.finish()
    }

    @Test
    fun finishMoreInfo() {
        val byteArray = byteArrayOf(0b00000010, 0b01000100)
        val sut = ByteDeconstructor(byteArray)
        sut.readInt(10)

        assertFailsWith(InvalidPacketException::class) {
            sut.finish()
        }
    }

    @Test
    fun finishAnotherPacket() {
        val byteArray = byteArrayOf(0b00000010, 0b01000000, 0b00000000)
        val sut = ByteDeconstructor(byteArray)
        sut.readInt(10)

        assertFailsWith(InvalidPacketException::class) {
            sut.finish()
        }
    }

    @Test
    fun finishAnotherPacketNotEmpty() {
        val byteArray = byteArrayOf(0b00000010, 0b01000000, 0b00001000)
        val sut = ByteDeconstructor(byteArray)
        sut.readInt(10)

        assertFailsWith(InvalidPacketException::class) {
            sut.finish()
        }
    }

}