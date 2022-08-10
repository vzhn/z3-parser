package me.vzhilin.gr.parser

sealed class DerivationValidationResult
object Ok: DerivationValidationResult() {
    override fun toString() = "OK"
}
data class BadReplacement(val lineNumber: Int): DerivationValidationResult()
data class UnexpectedNonTerm(val nt: NonTerminalDerivation): DerivationValidationResult()
data class LastProductionShouldHaveOneNt(val n: Int): DerivationValidationResult()
data class UnexpectedTerm(val lineNumber: Int, val t: TerminalDerivation): DerivationValidationResult()
data class BadProdReplacement(val rule: Prod, val symbols: List<DerivationSymbol>): DerivationValidationResult()
data class ImproperProdSymbol(val rule: Prod, val expected: Rule, val got: Rule): DerivationValidationResult()
data class ImproperProdDerivation(val expected: List<DerivationSymbol>, val got: List<DerivationSymbol>):
    DerivationValidationResult()
data class BadSumDerivationRange(val ir: IntRange): DerivationValidationResult()
data class SumComponentWasNotFound(val lineNumber: Int, val sum: Sum, val probe: Rule): DerivationValidationResult()

class DerivationValidator {
    private fun result(rs: List<DerivationValidationResult>): DerivationValidationResult {
        return rs.firstOrNull { it != Ok } ?: Ok
    }

    fun validate(steps: List<DerivationStep>): DerivationValidationResult {
        return result(listOf(
            firstRuleIsTermOnly(steps.first()),
            lastRuleIsNonTerm(steps.last() as DerivationStep.Tail, steps.lastIndex),
            checkIndividualSteps(steps)
        ))
    }

    private fun firstRuleIsTermOnly(s: DerivationStep): DerivationValidationResult {
        val nonTerminal = (s as DerivationStep.Middle).input.filterIsInstance<NonTerminalDerivation>()
        if (nonTerminal.isNotEmpty()) {
            return UnexpectedNonTerm(nonTerminal[0])
        }
        return Ok
    }

    private fun lastRuleIsNonTerm(s: DerivationStep.Tail, lineNumber: Int): DerivationValidationResult {
        if (s.input.size != 1) {
            return LastProductionShouldHaveOneNt(s.input.size)
        }
        val symbol = s.input.first()
        if (symbol is TerminalDerivation) {
            return UnexpectedTerm(lineNumber, symbol)
        }
        return Ok
    }

    private fun checkIndividualSteps(steps: List<DerivationStep>): DerivationValidationResult {
        return result(steps.zipWithNext().mapIndexed  {
            index, (a, b) ->
            val left = a as DerivationStep.Middle
            checkReplacement(left.input, left.substitutions, b.input, index)
        })
    }

    private fun checkReplacement(
        left: List<DerivationSymbol>,
        substitutions: List<Pair<Rule, IntRange>>,
        right: List<DerivationSymbol>,
        lineNumber: Int
    ): DerivationValidationResult {
        var line = left
        var diff = 0
        substitutions.forEach { (rule, range) ->
            line = left.replaceBy(rule as NonTerm, IntRange(diff + range.first, diff + range.last))
            diff += range.last - range.first
        }
        if (line == right) {
            return Ok
        } else {
            return BadReplacement(lineNumber)
        }

//        var
//
//        return when (rule) {
//            is Prod -> checkProdDerivation(lineNumber, left, rule, range, right)
//            is Sum -> checkSumDerivation(lineNumber, left, rule, range, right)
//            is Term -> UnexpectedTermRule(lineNumber, rule)
//        }
    }

    private fun checkProdDerivation(
        lineNumber: Int,
        left: List<DerivationSymbol>,
        rule: Prod,
        range: IntRange,
        right: List<DerivationSymbol>
    ): DerivationValidationResult {
        val symbols = left.subList(range.first, range.last + 1)

        // 1: number of symbols == number of prod sub-rules
        if (symbols.size != rule.components.size) {
            return BadProdReplacement(rule, symbols)
        }

        // 2: each of symbol's rule is matching prod sub-rule
        // symbol.rule = prod.args[symbol.index]
        val notMatched = symbols.zip(rule.components).firstOrNull { (symbol, rule) ->
            symbol.rule != rule
        }
        if (notMatched != null) {
            return ImproperProdSymbol(rule, notMatched.second, notMatched.first.rule)
        }

        // 3: replacing ..., S1, ..., SN, ...
        //              ..., rule       , ...
        val replaced = left.replaceBy(rule, range)
        if (right != replaced) {
            return ImproperProdDerivation(right, replaced)
        }
        return Ok
    }

    private fun checkSumDerivation(
        lineNumber: Int,
        left: List<DerivationSymbol>,
        rule: Sum,
        range: IntRange,
        right: List<DerivationSymbol>
    ): DerivationValidationResult {
        val symbols = left.subList(range.first, range.last + 1)
        // 1. check that range has only one symbol
        if (range.last != range.first || symbols.size != 1) {
            return BadSumDerivationRange(range)
        }

        val ds = symbols.first()

        // 2. check that sum has option == rule
        rule.components.firstOrNull { it == ds.rule }
            ?: return SumComponentWasNotFound(lineNumber, rule, ds.rule)

        // 3. replacing option to rule
        val replaced = left.replaceBy(rule, range)
        if (right != replaced) {
            return ImproperProdDerivation(right, replaced)
        }
        return Ok
    }

    private fun getText(symbols: List<DerivationSymbol>): String {
        return symbols.joinToString("") {
            when (it) {
                is NonTerminalDerivation -> it.word
                is TerminalDerivation ->  it.rule.ch.toString()
            }
        }
    }

    private fun List<DerivationSymbol>.replaceBy(rule: NonTerm, range: IntRange): List<DerivationSymbol> {
        val symbols = filterIndexed { index, _ -> range.contains(index) }
        val removed = filterIndexed { index, _ -> !range.contains(index) }
        val newRule = NonTerminalDerivation(rule, getText(symbols))

        val list = removed.toMutableList()
        list.add(range.first, newRule)

        return list
    }
}
