package me.vzhilin.gr.rules

// ['a', 'b']::App
// ['λ', 'x', '.', 'y']::Abst
// ['c']::TermC
// ['c']::V
// ['c']::T

data class Group(val rule: Rule, val symbols: List<Term>)
data class DerivationLine(val groups: List<Group>)
data class Derivation(val lines: DerivationLine)