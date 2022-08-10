package me.vzhilin.gr.parser

import me.vzhilin.gr.rules.DerivationStep
import me.vzhilin.gr.rules.NonTerminalDerivation
import me.vzhilin.gr.rules.TerminalDerivation
import me.vzhilin.gr.rules.parseDerivation
import kotlin.test.Test
import kotlin.test.assertEquals

class DerivationParserTests {
    @Test
    fun test() {
        val g = simpleGrammar()
        val derivationA = g.parseDerivation("""
            'ab' # V(0) V(1)
            V('a') V('b')
        """.trimIndent())

        val derivationB = g.parseDerivation("""
            'a' 'b' # V(0:0) V(1)
            V('a') V('b')
        """.trimIndent())

        val expected = listOf(
            DerivationStep.Middle(
                listOf(
                    TerminalDerivation(g['a']),
                    TerminalDerivation(g['b'])
                ),
                listOf(
                    g["V"] to 0..0,
                    g["V"] to 1..1
                )
            ),
            DerivationStep.Tail(
                listOf(
                    NonTerminalDerivation(g["V"], "a"),
                    NonTerminalDerivation(g["V"], "b")
                )
            )
        )
        assertEquals(expected, derivationA)
        assertEquals(expected, derivationB)
    }
}