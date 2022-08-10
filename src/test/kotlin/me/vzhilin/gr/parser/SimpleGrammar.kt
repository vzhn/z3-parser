package me.vzhilin.gr.parser

import kotlin.test.Test
import kotlin.test.assertIs

fun simpleGrammar(): Grammar {
    return Grammar.of(
        "T = V | APP | ABST",
        "V = 'x' | 'y' | 'a' | 'b'" ,
        "APP = T T",
        "ABST = 'λ' V '.' T"
    )
}

class GrammarTests {
    @Test
    fun test1() {
        val g = Grammar.of(
            "A = 'aaa'",
            "B = 'bbb'",
            "S = A | B"
        )
        val a = g["A"] as Prod
        val b = g["B"] as Prod
        val aTerms = a.components.filterIsInstance<Term>()
        val bTerms = b.components.filterIsInstance<Term>()
        assert(aTerms.all { it.ch == 'a' })
        assert(bTerms.all { it.ch == 'b' })
    }

    @Test
    fun test2() {
        val g = simpleGrammar()
        assertIs<Term>(g['λ'])
    }
}