package me.vzhilin.gr.parser

import kotlin.test.Test

class LimitsTest {
    @Test
    fun test() {
        val g = simpleGrammar()
        val cl = ComputeLimits(g)

        for (inputLength in 1 until 10) {
            println("$inputLength: ${cl.computeTreeHeights(g["T"] as NonTerm, inputLength)}")
        }
    }
}