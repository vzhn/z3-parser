package me.vzhilin.gr.derivation

import me.vzhilin.gr.rules.DerivationStep
import me.vzhilin.gr.rules.NonTerminalSymbol
import me.vzhilin.gr.rules.TerminalSymbol
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
                    TerminalSymbol('a', g.termFor('a')),
                    TerminalSymbol('b', g.termFor('b'))
                ),
                g.find("A"),
                0..0,
                listOf(
                    NonTerminalSymbol(g.find("A"), "a"),
                    TerminalSymbol('b', g.termFor('b'))
                )
            )
        )
        assertEquals(expected, derivationA)
        assertEquals(expected, derivationB)
    }
}