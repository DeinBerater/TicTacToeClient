package game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FieldCoordinateTest {

    @Test
    fun checkX() {
        val sut = FieldCoordinate(1, 2)

        val have = sut.x
        val want = 1

        assertEquals(want, have)
    }

    @Test
    fun checkY() {
        val sut = FieldCoordinate(1, 2)

        val have = sut.y
        val want = 2

        assertEquals(want, have)
    }

    @Test
    fun createInvalid() {
        assertFailsWith(IllegalArgumentException::class) {
            FieldCoordinate(1, 3)
        }
    }

    @Test
    fun createInvalid2() {
        assertFailsWith(IllegalArgumentException::class) {
            FieldCoordinate(-1, 2)
        }
    }

    @Test
    fun createWithIndex1() {
        val sut = FieldCoordinate(7)

        val have = sut.x
        val want = 1

        assertEquals(want, have)
    }

    @Test
    fun createWithIndex2() {
        val sut = FieldCoordinate(7)

        val have = sut.y
        val want = 2

        assertEquals(want, have)
    }

    @Test
    fun createInvalidIndex() {
        assertFailsWith(IllegalArgumentException::class) {
            FieldCoordinate(9)
        }
    }

    @Test
    fun createInvalidIndex2() {
        assertFailsWith(IllegalArgumentException::class) {
            FieldCoordinate(-1)
        }
    }
}