package me.vzhilin.gr.derivation

import me.vzhilin.gr.rules.DerivationStep
import me.vzhilin.gr.rules.Grammar
import me.vzhilin.gr.rules.NonTerminalSymbol
import me.vzhilin.gr.rules.Prod
import me.vzhilin.gr.rules.Ref
import me.vzhilin.gr.rules.Rule
import me.vzhilin.gr.rules.Sum
import me.vzhilin.gr.rules.Symbol
import me.vzhilin.gr.rules.Term
import me.vzhilin.gr.rules.TerminalSymbol
import me.vzhilin.gr.rules.parseDerivation
import me.vzhilin.gr.simpleGrammar
import kotlin.test.Test
import kotlin.test.assertEquals

class DerivationValidationTests {
    private val input =
"""     λ    x . x    λ    y . y      y  #  X(1)                       # λ X(x) . x    λ    y . y      y 
        λ X(x) . x    λ    y . y      y  #  Y(5)                       # λ X(x) . X(x) λ Y(y) . y      y 
        λ X(x) . X(x) λ Y(y) . y      y  #  Y(7)                       # λ X(x) . X(x) λ Y(y) . Y(y)   y 
        λ X(x) . X(x) λ Y(y) . Y(y)   y  #  Y(8)                       # λ X(x) . X(x) λ Y(y) . Y(y) Y(y)
        λ X(x) . X(x) λ Y(y) . Y(y) Y(y) #  V(3)                       # λ X(x) . V(x) λ Y(y) . Y(y) Y(y)
        λ X(x) . V(x) λ Y(y) . Y(y) Y(y) #  T(3)                       # λ X(x) . T(x) λ Y(y) . Y(y) Y(y)
        λ X(x) . T(x) λ Y(y) . Y(y) Y(y) #  V(1)                       # λ V(x) . T(x) λ Y(y) . Y(y) Y(y)
        λ V(x) . T(x) λ Y(y) . Y(y) Y(y) #  ABST(0:3)                  # ABST(λx.x)    λ Y(y) . Y(y) Y(y)
        ABST(λx.x)    λ Y(y) . Y(y) Y(y) #  V(2)                       # ABST(λx.x)    λ V(y) . Y(y) Y(y)
        ABST(λx.x)    λ V(y) . Y(y) Y(y) #  V(4)                       # ABST(λx.x)    λ V(y) . V(y) Y(y)
        ABST(λx.x)    λ V(y) . V(y) Y(y) #  T(4)                       # ABST(λx.x)    λ V(y) . T(y) Y(y)
        ABST(λx.x)    λ V(y) . T(y) Y(y) #  ABST(1:4)                  # ABST(λx.x)    ABST(λy.y)    Y(y)
        ABST(λx.x)    ABST(λy.y)    Y(y) #  T(0)                       # T(λx.x)       ABST(λy.y)    Y(y)
        T(λx.x)       ABST(λy.y)    Y(y) #  T(1)                       # T(λx.x)       T(λy.y)       Y(y)
        T(λx.x)       T(λy.y)       Y(y) #  APP(0:1)                   # APP(λx.xλy.y)               Y(y)
        APP(λx.xλy.y)               Y(y) #  T(0)                       # T(λx.xλy.y)                 Y(y)
        T(λx.xλy.y)                 Y(y) #  T(1)                       # T(λx.xλy.y)                 T(y)
        T(λx.xλy.y)                 T(y) #  APP(0:1)                   # APP(λx.xλy.yy)                  
        APP(λx.xλy.yy)                   #  T(0)                       # T(λx.xλy.yy)
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
data class UnexpectedNonTerm(val nt: NonTerminalSymbol): DerivationValidationResult()
data class UnexpectedNonTermRule(val nt: Rule): DerivationValidationResult()
data class LastProductionShouldHaveOneNt(val n: Int): DerivationValidationResult()
data class UnexpectedTerm(val t: TerminalSymbol): DerivationValidationResult()
data class UnexpectedTermRule(val lineNumber: Int, val t: Term): DerivationValidationResult()
data class UnexpectedRefRule(val ref: Ref): DerivationValidationResult()
data class BadChaining(
    val badChainedIndex: Int,
    val symbolIndex: Int,
    val left: Symbol,
    val right: Symbol
) : DerivationValidationResult()
data class SizeNotMatched(val lineNumber: Int): DerivationValidationResult()
data class BadStep(val left: List<Symbol>, val right: List<Symbol>): DerivationValidationResult()
data class BadProdReplacement(val rule: Prod, val symbols: List<Symbol>):DerivationValidationResult()
data class ImproperProdSymbol(val rule: Prod, val expected: Rule, val got: Rule): DerivationValidationResult()
data class ImproperProdDerivation(val expected: List<Symbol>, val got: List<Symbol>):DerivationValidationResult()
data class BadSumDerivationRange(val ir: IntRange): DerivationValidationResult()
data class SumComponentWasNotFound(val sum: Sum, val probe: Rule): DerivationValidationResult()

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
        val nonTerminal = s.input.firstOrNull { it is NonTerminalSymbol } as NonTerminalSymbol?
        if (nonTerminal != null) {
            return UnexpectedNonTerm(nonTerminal)
        }

        val nonTerminalRule = s.input.map(Symbol::rule).firstOrNull{ it !is Term }
        if (nonTerminalRule != null) {
            return UnexpectedNonTermRule(nonTerminalRule)
        }
        return Ok
    }

    private fun lastRuleIsNonTerm(s: DerivationStep, lineNumber: Int): DerivationValidationResult {
        if (s.result.size != 1) {
            return LastProductionShouldHaveOneNt(s.result.size)
        }
        val lastSymbol = s.result.first()
        if (lastSymbol is TerminalSymbol) {
            return UnexpectedTerm(lastSymbol)
        }
        val lastRule = lastSymbol.rule
        if (lastRule is Term) {
            return UnexpectedTermRule(lineNumber, lastRule)
        }
        if (lastRule is Ref) {
            return UnexpectedRefRule(lastRule)
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

    private fun checkLeftRightHasSameSymbols(left: List<Symbol>, right: List<Symbol>): DerivationValidationResult {
        if (getText(left) != getText(right)) {
            return BadStep(left, right)
        }
        return Ok
    }

    private fun checkReplacement(
        left: List<Symbol>,
        rule: Rule,
        range: IntRange,
        right: List<Symbol>,
        lineNumber: Int
    ): DerivationValidationResult {
        return when (rule) {
            is Prod -> {
                checkProdDerivation(left, rule, range, right)
            }
            is Sum -> {
                checkSumDerivation(left, rule, range, right)
            }
            is Ref -> UnexpectedRefRule(rule)
            is Term -> UnexpectedTermRule(lineNumber, rule)
        }
    }

    private fun checkProdDerivation(
        left: List<Symbol>,
        rule: Prod,
        range: IntRange,
        right: List<Symbol>
    ): DerivationValidationResult {
        val symbols = left.subList(range.first, range.last + 1)

        // 1: number of symbols == number of prod sub-rules
        if (symbols.size != rule.args.size) {
            return BadProdReplacement(rule, symbols)
        }

        // 2: each of symbol's rule is matching prod sub-rule
        // symbol.rule = prod.args[symbol.index]
        val notMatched = symbols.zip(rule.args.map(g::resolve)).firstOrNull { (symbol, rule) ->
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
        left: List<Symbol>,
        rule: Sum,
        range: IntRange,
        right: List<Symbol>
    ): DerivationValidationResult {
        val symbols = left.subList(range.first, range.last + 1)
        // 1. check that range has only one symbol
        if (range.last != range.first || symbols.size != 1) {
            return BadSumDerivationRange(range)
        }

        val symbol = symbols.first()

        // 2. check that sum has option == rule
        val option = rule.args.map { g.resolve(it) }.firstOrNull { it == symbol.rule }
            ?: return SumComponentWasNotFound(rule, symbol.rule)

        // 3. replacing option to rule
        val replaced = left.replaceBy(rule, range)
        if (right != replaced) {
            return ImproperProdDerivation(right, replaced)
        }
        return Ok
    }

    private fun getText(symbols: List<Symbol>): String {
        return symbols.joinToString("") {
            when (it) {
                is NonTerminalSymbol -> it.text
                is TerminalSymbol ->  it.char.toString()
            }
        }
    }

    private fun List<Symbol>.replaceBy(rule: Rule, range: IntRange): List<Symbol> {
        val symbols = filterIndexed { index, _ -> range.contains(index) }
        val removed = filterIndexed { index, _ -> !range.contains(index) }
        val newRule = NonTerminalSymbol(rule, getText(symbols))

        val list = removed.toMutableList()
        list.add(range.first, newRule)

        return list
    }
}
