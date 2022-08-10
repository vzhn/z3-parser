package me.vzhilin.gr.parser

import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceRangeTests {
    @Test
    fun testReplaceRange1() {
        val a = "abc".toList()
        val b = "aXc".toList()
        assertEquals(b, replaceRange(a, 1..1, "X".toList()))
    }

    @Test
    fun testReplaceRange2() {
        val a = "abc".toList()
        val b = "ab".toList()
        assertEquals(b, replaceRange(a, 1..2, "b".toList()))
    }

    @Test
    fun testReplaceRange3() {
        val a = "abc".toList()
        val b = "".toList()
        assertEquals(b, replaceRange(a, 0..2, "".toList()))
    }

    @Test
    fun findReplacement1() {
        val a = "abc".toList()
        val b = "aXc".toList()
        assertEquals(1..1 to "X".toList(), findReplacement(a, b))
    }

    @Test
    fun findReplacement2() {
        val a = "abc".toList()
        val b = "ab".toList()
        assertEquals(1..2 to "b".toList(), findReplacement(a, b))
    }
}

fun <T> replaceRange(original: List<T>, r: IntRange, replacement: List<T>): List<T> {
    val left = original.subList(0, r.first)
    val right = original.subList(r.last + 1, original.size)
    return left + replacement + right
}

fun <T> findReplacement(original: List<T>, result: List<T>): Pair<IntRange, List<T>> {
    val neq: (Pair<T, T>) -> Boolean = { (a, b) -> a != b }
    var leftIndex = original.zip(result).indexOfFirst(neq)
    if (leftIndex == -1) {
        leftIndex = minOf(original.size, result.size) - 1
    }
    val reversedIndex = original.reversed().zip(result.reversed()).indexOfFirst(neq)
    val originalRange = leftIndex..(original.size - 1 - reversedIndex)
    val resultRange = leftIndex..(result.size - 1 - reversedIndex)

    return originalRange to result.subList(resultRange.first, resultRange.last + 1)
}

