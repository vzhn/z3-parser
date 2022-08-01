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
            a b # A(0) b # A(a) b
        """.trimIndent())

        val derivationB = g.parseDerivation("""
            a b # A(0:0) b # A(a) b
        """.trimIndent())

        val expected = listOf(
            DerivationStep(
                listOf(
                    TerminalDerivation(g.getTerm('a')),
                    TerminalDerivation(g.getTerm('b'))
                ),
                g.get("A"),
                0..0,
                listOf(
                    NonTerminalDerivation(g.get("A"), "a"),
                    TerminalDerivation(g.getTerm('b'))
                )
            )
        )
        assertEquals(expected, derivationA)
        assertEquals(expected, derivationB)
    }
}