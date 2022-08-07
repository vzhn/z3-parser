package me.vzhilin.gr

import me.vzhilin.gr.rules.ComputeLimits
import kotlin.test.Test

class LimitsTest {
    @Test
    fun test() {
        val g = simpleGrammar()
        val cl = ComputeLimits(g)
        for (i in 0 until 10) {
            println(cl.next())
        }
    }
}