package me.vzhilin.gr.derivation

import me.vzhilin.gr.rules.DerivationStep
import me.vzhilin.gr.rules.DerivationSymbol
import me.vzhilin.gr.rules.Grammar
import me.vzhilin.gr.rules.NonTerm
import me.vzhilin.gr.rules.NonTerminalDerivation
import me.vzhilin.gr.rules.Prod
import me.vzhilin.gr.rules.Sum
import me.vzhilin.gr.rules.Rule
import me.vzhilin.gr.rules.Term
import me.vzhilin.gr.rules.TerminalDerivation
import me.vzhilin.gr.rules.parseDerivation
import me.vzhilin.gr.simpleGrammar

import kotlin.test.Test
import kotlin.test.assertEquals

class DerivationValidationTests {
    private val input =
    """ λ x . x λ y . y y            #  V(3)         # λ x . V(x) λ y . y y
        λ x . V(x) λ y . y y         #  T(3)         # λ x . T(x) λ y . y y
        λ x . T(x) λ y . y y         #  V(1)         # λ V(x) . T(x) λ y . y y
        λ V(x) . T(x) λ y . y y      #  ABST(0:3)    # ABST(λx.x) λ y . y y
        ABST(λx.x) λ y . y y         #  V(2)         # ABST(λx.x) λ V(y) . y y
        ABST(λx.x) λ V(y) . y y      #  V(4)         # ABST(λx.x) λ V(y) . V(y) y
        ABST(λx.x) λ V(y) . V(y) y   #  T(4)         # ABST(λx.x) λ V(y) . T(y) y
        ABST(λx.x) λ V(y) . T(y) y   #  ABST(1:4)    # ABST(λx.x) ABST(λy.y) y
        ABST(λx.x) ABST(λy.y) y      #  T(0)         # T(λx.x) ABST(λy.y) y
        T(λx.x) ABST(λy.y) y         #  T(1)         # T(λx.x) T(λy.y) y
        T(λx.x) T(λy.y) y            #  APP(0:1)     # APP(λx.xλy.y) y
        APP(λx.xλy.y) y              #  T(0)         # T(λx.xλy.y) y
        T(λx.xλy.y) y                #  V(1)         # T(λx.xλy.y) V(y)
        T(λx.xλy.y) V(y)             #  T(1)         # T(λx.xλy.y) T(y)
        T(λx.xλy.y) T(y)             #  APP(0:1)     # APP(λx.xλy.yy)                  
        APP(λx.xλy.yy)               #  T(0)         # T(λx.xλy.yy)
    """.trimIndent()

    @Test
    fun test() {
        val g = simpleGrammar()
        val derivation = g.parseDerivation(input)

        assertEquals(Ok, DerivationValidator(g).validate(derivation))
    }
}

sealed class DerivationValidationResult
object Ok: DerivationValidationResult() {
    override fun toString() = "OK"
}
data class UnexpectedNonTerm(val nt: NonTerminalDerivation): DerivationValidationResult()
data class LastProductionShouldHaveOneNt(val n: Int): DerivationValidationResult()
data class UnexpectedTerm(val lineNumber: Int, val t: TerminalDerivation): DerivationValidationResult()
data class UnexpectedTermRule(val lineNumber: Int, val t: Term): DerivationValidationResult()
data class BadChaining(
    val badChainedIndex: Int,
    val symbolIndex: Int,
    val left: DerivationSymbol,
    val right: DerivationSymbol
) : DerivationValidationResult()
data class SizeNotMatched(val lineNumber: Int): DerivationValidationResult()
data class BadStep(val left: List<DerivationSymbol>, val right: List<DerivationSymbol>): DerivationValidationResult()
data class BadProdReplacement(val rule: Prod, val symbols: List<DerivationSymbol>):DerivationValidationResult()
data class ImproperProdSymbol(val rule: Prod, val expected: Rule, val got: Rule): DerivationValidationResult()
data class ImproperProdDerivation(val expected: List<DerivationSymbol>, val got: List<DerivationSymbol>):DerivationValidationResult()
data class BadSumDerivationRange(val ir: IntRange): DerivationValidationResult()
data class SumComponentWasNotFound(val lineNumber: Int, val sum: Sum, val probe: Rule): DerivationValidationResult()

class DerivationValidator(val g: Grammar) {
    private fun result(rs: List<DerivationValidationResult>): DerivationValidationResult {
        return rs.firstOrNull { it != Ok } ?: Ok
    }

    fun validate(steps: List<DerivationStep>): DerivationValidationResult {
        return result(listOf(
            firstRuleIsTermOnly(steps.first()),
            lastRuleIsNonTerm(steps.last(), steps.lastIndex),
            stepsAreChainedProperly(steps),
            checkIndividualSteps(steps)
        ))
    }

    private fun firstRuleIsTermOnly(s: DerivationStep): DerivationValidationResult {
        val nonTerminal = s.input.filterIsInstance<NonTerminalDerivation>()
        if (nonTerminal.isNotEmpty()) {
            return UnexpectedNonTerm(nonTerminal[0])
        }
        return Ok
    }

    private fun lastRuleIsNonTerm(s: DerivationStep, lineNumber: Int): DerivationValidationResult {
        if (s.result.size != 1) {
            return LastProductionShouldHaveOneNt(s.result.size)
        }
        val lastSymbol = s.result.first()
        if (lastSymbol is TerminalDerivation) {
            return UnexpectedTerm(lineNumber, lastSymbol)
        }
        return Ok
    }

    private fun stepsAreChainedProperly(steps: List<DerivationStep>): DerivationValidationResult {
        val indexOfNotMatchedSize = steps.zipWithNext { lhs, rhs ->
            lhs.result to rhs.input
        }.indexOfFirst { (lhs, rhs) ->
            lhs.size != rhs.size
        }

        if (indexOfNotMatchedSize != -1) {
            return SizeNotMatched(indexOfNotMatchedSize)
        }

        val badStep = steps.zipWithNext { lhs, rhs ->
            lhs.result to rhs.input
        }.indexOfFirst { (lhs, rhs) ->
            lhs != rhs
        }

        if (badStep != -1) {
            val step = steps[badStep]
            val pairs = step.input.zip(step.result)
            val badPosition = pairs.indexOfFirst { (lhs, rhs) -> lhs != rhs }
            val (left, right) = pairs[badPosition]
            return BadChaining(badStep, badPosition, left, right)
        }
        return Ok
    }

    private fun checkIndividualSteps(steps: List<DerivationStep>): DerivationValidationResult {
        return result(steps.flatMapIndexed { index, s ->
            val (left, rule, range, right) = s
            listOf(
                checkLeftRightHasSameSymbols(left, right),
                checkReplacement(left, rule, range, right, index)
            )
        })
    }

    private fun checkLeftRightHasSameSymbols(left: List<DerivationSymbol>, right: List<DerivationSymbol>): DerivationValidationResult {
        if (getText(left) != getText(right)) {
            return BadStep(left, right)
        }
        return Ok
    }

    private fun checkReplacement(
        left: List<DerivationSymbol>,
        rule: Rule,
        range: IntRange,
        right: List<DerivationSymbol>,
        lineNumber: Int
    ): DerivationValidationResult {
        return when (rule) {
            is Prod -> {
                checkProdDerivation(lineNumber, left, rule, range, right)
            }
            is Sum -> {
                checkSumDerivation(lineNumber, left, rule, range, right)
            }
            is Term -> UnexpectedTermRule(lineNumber, rule)
        }
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
        val option = rule.components.firstOrNull { it == ds.rule }
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
