package communication

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ByteBuilderTest {

    @Test
    fun empty() {
        val sut = ByteBuilder()
        val want = byteArrayOf().asList()

        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }


    @Test
    fun addBoolean() {
        val sut = ByteBuilder()
        val want = byteArrayOf(-128).asList()

        val have = sut.addBoolean(true).getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addBoolean2() {
        val sut = ByteBuilder()
        val want = byteArrayOf(0).asList()

        val have = sut.addBoolean(false).getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addBooleanEightTimes() {
        val sut = ByteBuilder()
        val want = byteArrayOf(-1).asList()

        for (i in 1..8) {
            sut.addBoolean(true)
        }
        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addBooleanLast() {
        val sut = ByteBuilder()
        val want = byteArrayOf(0, 1).asList()

        for (i in 1..15) {
            sut.addBoolean(false)
        }
        sut.addBoolean(true)
        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addBooleanFirstInSecondByte() {
        val sut = ByteBuilder()
        val want = byteArrayOf(0, -128).asList()

        for (i in 1..8) {
            sut.addBoolean(false)
        }
        sut.addBoolean(true)
        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addSmallInt() {
        val sut = ByteBuilder()
        val want = byteArrayOf(0b11000000.toByte()).asList()

        sut.addInt(3, 2)
        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addIntAtEnd() {
        val sut = ByteBuilder()
        val want = byteArrayOf(0b00000101).asList()

        sut.addInt(5, 8)
        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addInt() {
        val sut = ByteBuilder()
        val want = byteArrayOf(0b00001010).asList()

        sut.addInt(5, 7)
        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addIntOverlapping() {
        val sut = ByteBuilder()
        val want = byteArrayOf(0b00000010, 0b01000000).asList()

        sut.addInt(9, 10)
        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addIntMultiple() {
        val sut = ByteBuilder()
        val want = byteArrayOf(0b01010101, 0b01010101).asList()

        for (i in 1..4) {
            sut.addInt(5, 4)
        }
        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addIntMultipleAndBoolean() {
        val sut = ByteBuilder()
        val want = byteArrayOf(0b01010101, 0b01010101, 0).asList()

        for (i in 1..4) {
            sut.addInt(5, 4)
        }
        sut.addBoolean(false)
        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addIntMultipleAndBoolean2() {
        val sut = ByteBuilder()
        val want = byteArrayOf(0b01010101, 0b01010101, -128).asList()

        for (i in 1..4) {
            sut.addInt(5, 4)
        }
        sut.addBoolean(true)
        val have = sut.getBytes().asList()

        assertEquals(want, have)
    }

    @Test
    fun addIntTooBig() {
        val sut = ByteBuilder()

        assertFailsWith(IllegalArgumentException::class) {
            sut.addInt(10, 2)
        }
    }
}