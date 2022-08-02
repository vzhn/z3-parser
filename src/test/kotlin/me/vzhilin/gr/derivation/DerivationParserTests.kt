package me.vzhilin.gr.derivation

import me.vzhilin.gr.rules.DerivationStep
import me.vzhilin.gr.rules.NonTerminalDerivation
import me.vzhilin.gr.rules.TerminalDerivation
import me.vzhilin.gr.rules.parseDerivation
import me.vzhilin.gr.simpleGrammar
import kotlin.test.Test
import kotlin.test.assertEquals

class DerivationParserTests {
    @Test
    fun test() {
        val g = simpleGrammar()
        val derivationA = g.parseDerivation("""
            'ab' # V(0)
            V('a') 'b'
        """.trimIndent())

        val derivationB = g.parseDerivation("""
            'a' 'b' # V(0:0)
            V('a') 'b'
        """.trimIndent())

        val expected = listOf(
            DerivationStep.Middle(
                listOf(
                    TerminalDerivation(g['a']),
                    TerminalDerivation(g['b'])
                ),
                g["V"],
                0..0
            ),
            DerivationStep.Tail(
                listOf(
                    NonTerminalDerivation(g["V"], "a"),
                    TerminalDerivation(g['b'])
                )
            )
        )
        assertEquals(expected, derivationA)
        assertEquals(expected, derivationB)
    }
}