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

sealed class DerivationStep {
    abstract val input: List<DerivationSymbol>
    data class Middle(
        override val input: List<DerivationSymbol>,
        val substitutions: List<Pair<Rule, IntRange>>
    ): DerivationStep()

    data class Tail(
        override val input: List<DerivationSymbol>
    ): DerivationStep()
}

fun Grammar.parseDerivation(input: String): List<DerivationStep> {
    return input.split('\n').map { line ->
        if (line.contains('#')) {
            val (left, middle) = line.split('#')
            val leftSymbols = parseSymbols(left.trim())
            DerivationStep.Middle(leftSymbols, parseMiddle(middle.trim()))
        } else {
            DerivationStep.Tail(parseSymbols(line.trim()))
        }
    }
}

private fun Grammar.parseMiddle(input: String): List<Pair<Rule, IntRange>> {
    return input.split(' ').map { word ->
        val p1 = word.indexOf('(')
        val p2 = word.indexOf(')')
        val range = word.substring(p1 + 1, p2)
        val name = word.substring(0, p1)
        val r = if (range.contains(':')) {
            val a = range.substring(0, range.indexOf(':')).toInt()
            val b = range.substring(range.indexOf(':') + 1).toInt()
            a..b
        } else {
            val a = range.toInt()
            a..a
        }
        get(name) to r
    }
}

private fun Grammar.parseSymbols(input: String): List<DerivationSymbol> {
    val inp = input.split(Regex("\\s+"))
    return inp.flatMap { word ->
        if (word.startsWith('\'')) {
            if (word.length < 3 || !word.endsWith('\''))
                throw IllegalArgumentException("expected '$word' to be term string")

            word.substring(1, word.length - 1).map { char ->
                TerminalDerivation(this[char])
            }
        } else {
            val p1 = word.indexOf('(')
            val p2 = word.indexOf(')')
            val chars = word.substring(p1 + 1, p2)
            if (chars.length < 3 || !chars.endsWith('\''))
                throw IllegalArgumentException("expected '$chars' to be term string")

            val name = word.substring(0, p1)
            val rule = get(name)
            listOf(NonTerminalDerivation(rule, chars.substring(1 until chars.lastIndex)))
        }
    }
}
