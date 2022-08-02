package me.vzhilin.gr

import me.vzhilin.gr.rules.Grammar
import me.vzhilin.gr.rules.Prod
import me.vzhilin.gr.rules.Term
import kotlin.test.Test

fun simpleGrammar(): Grammar {
    return Grammar.of(
        "T = V | APP | ABST",
        "V = 'x' | 'y' | 'a' | 'b'" ,
        "APP = T T",
        "ABST = 'λ' V '.' T"
    )
}

class GrammarTests() {
    @Test
    fun test() {
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
}