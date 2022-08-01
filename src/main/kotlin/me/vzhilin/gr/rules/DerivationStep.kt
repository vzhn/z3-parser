package me.vzhilin.gr.rules

sealed class DerivationSymbol {
    abstract val rule: Rule
}
data class TerminalDerivation(override val rule: Term): DerivationSymbol()
data class NonTerminalDerivation(override val rule: Rule, val word: String): DerivationSymbol() {
    init {
        if (word.isEmpty()) {
            throw IllegalStateException("Blank rule: '$rule'")
        }
    }
}
data class DerivationStep(
    val input: List<DerivationSymbol>,
    val substitution: Rule,
    val range: IntRange,
    val result: List<DerivationSymbol>
)

// a b         # V(0)     # V(a) b
// V(a) b      # V(1)     # V(a) V(b)
// V(a) V(b)   # APP(0:1) # APP(ab)
// APP(ab)     # T(0)     # T(ab)
fun Grammar.parseDerivation(input: String): List<DerivationStep> {
    return input.split('\n').map { line ->
        val (left, middle, right) = line.split('#')
        val leftSymbols = parseSymbols(left.trim())
        val rightSymbols = parseSymbols(right.trim())
        val (rule, range) = parseMiddle(middle.trim())
        DerivationStep(leftSymbols, rule, range, rightSymbols)
    }
}

private fun Grammar.parseMiddle(input: String): Pair<Rule, IntRange> {
    val p1 = input.indexOf('(')
    val p2 = input.indexOf(')')
    val range = input.substring(p1 + 1, p2)
    val name = input.substring(0, p1)
    val r = if (range.contains(':')) {
        val a = range.substring(0, range.indexOf(':')).toInt()
        val b = range.substring(range.indexOf(':') + 1).toInt()
        a..b
    } else {
        val a = range.toInt()
        a..a
    }
    return get(name) to r
}

private fun Grammar.parseSymbols(input: String): List<DerivationSymbol> {
    val inp = input.split(Regex("\\s+"))
    return inp.map { word ->
        if (word.length == 1) {
            val char = word[0]
            TerminalDerivation(getTerm(char))
        } else {
            val p1 = word.indexOf('(')
            val p2 = word.indexOf(')')
            val chars = word.substring(p1 + 1, p2)
            val name = word.substring(0, p1)
            val rule = get(name)
            NonTerminalDerivation(rule, chars)
        }
    }
}
