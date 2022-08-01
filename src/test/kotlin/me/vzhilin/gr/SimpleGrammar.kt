package me.vzhilin.gr

import me.vzhilin.gr.rules.Grammar

fun simpleGrammar(): Grammar {
    return Grammar.of(
        "T = V | APP | ABST",
        "V = 'x' | 'y'",
        "APP = T T",
        "ABST = 'λ' V '.' T"
    )
}