package me.vzhilin.gr.derivation

import me.vzhilin.gr.rules.Grammar
import me.vzhilin.gr.rules.Rule

data class DerivationNode(val rule: Rule, val components: List<Rule>)
typealias Derivation = List<DerivationNode>

fun parseDerivation(g: Grammar, vararg lines : String): List<DerivationNode> {
    val symbols = lines.map { line -> parseGrammarSymbols(g, line) }
    symbols.zipWithNext().forEach { (left, right) ->
        if (left.size < right.size) {
            throw IllegalArgumentException("expected: ${left}.size < ${right}.size")
        }

        val index = left.zip(right).indexOfFirst { (a, b) -> a != b }
        if (index == -1 && left.size == right.size) {
            throw IllegalArgumentException("expected: ${left} != ${right}")
        }

        if (index == -1) {
            TODO()
        } else {
            TODO()
        }
    }
    TODO()
}

fun discoverSubstitution(
    left: List<Rule>,
    right: List<Rule>
): Pair<IntRange, Rule> {
    val minLen = minOf(left.size, right.size)
    TODO("Not yet implemented")
}

private fun parseGrammarSymbols(g: Grammar, line: String): List<Rule> {
    return line.split(' ').map(String::trim).flatMap { symbol ->
        if (symbol.startsWith("'")) {
            if (!symbol.endsWith("'")) {
                throw IllegalArgumentException("expected '$symbol' is term rule")
            }

            symbol.substring(1, symbol.length - 2).map { ch -> g[ch] }
        } else {
            listOf(g[symbol])
        }
    }
}

