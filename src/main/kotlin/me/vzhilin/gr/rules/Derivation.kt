package me.vzhilin.gr.rules

// ab         # V(0)     # V(a) b
// V(a) b     # V(1)     # V(a) V(b)
// V(a) V(b)  # APP(0:1) # APP(ab)
// APP(ab)    # T(0)     # T(ab)

sealed class Symbol {
    abstract val rule: Rule
}
data class TerminalSymbol(val char: Char, override val rule: Term): Symbol()
data class NonTerminalSymbol(override val rule: Rule, val text: String): Symbol()
data class Derivation(val s1: List<Symbol>, val r: Rule, val range: IntRange, val s2: List<Symbol>)

fun parse(input: String): Derivation {
    val parts = input.split('>')

    TODO()
}

fun parsePart(part: String, gr: Grammar): Word {
    val elements = part.split(' ')

    return Word(elements.flatMap {
        if (it[0] in 'A'..'Z') {
            parseNonTerminal(it)
        } else {
            parseTerminal(it, gr)
        }
    })
}

fun parseTerminal(input: String, gr: Grammar): List<TerminalSymbol> {
    return input.map { char ->
        val rule = gr.terms.first { it.value == char }
        TerminalSymbol(char, rule)
    }
}

fun parseNonTerminal(input: String): List<NonTerminalSymbol> {
    TODO()
}
/*

 */