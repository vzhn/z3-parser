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

class DerivationValidationTests {
    private val input = """
        λ . x x λ . y y                         # X(2)          # λ . X(x) x λ . y y
        λ . X(x) x λ . y y                      # X(3)          # λ . X(x) X(x) λ . y y
        λ . X(x) X(x) λ . y y                   # Y(6)          # λ . X(x) X(x) λ . Y(y) y
        λ . X(x) X(x) λ . Y(y) y                # Y(7)          # λ . X(x) X(x) λ . Y(y) Y(y)
        λ . X(x) X(x) λ . Y(y) Y(y)             # APP(6:7)      # λ . X(x) X(x) λ . APP(yy)
        λ . X(x) X(x) λ . APP(yy)               # T(6)          # λ . X(x) X(x) λ . T(yy)
        λ . X(x) X(x) λ . T(yy)                 # LAMBDA(4)     # λ . X(x) X(x) LAMBDA(λ) . T(yy)
        λ . X(x) X(x) LAMBDA(λ) . T(yy)         # DOT(5)        # λ . X(x) X(x) LAMBDA(λ) DOT(.) T(yy)
        λ . X(x) X(x) LAMBDA(λ) DOT(.) T(yy)    # ABST(4:6)     # λ . X(x) X(x) ABST(λ.yy) 
        λ . X(x) X(x) ABST(λ.yy)                # LAMBDA(0)     # LAMBDA(λ) . X(x) X(x) ABST(λ.yy) 
        LAMBDA(λ) . X(x) X(x) ABST(λ.yy)        # DOT(1)        # LAMBDA(λ) DOT(.) X(x) X(x) ABST(λ.yy)
        LAMBDA(λ) DOT(.) X(x) X(x) ABST(λ.yy)   # ABST(0:2)     # ABST(λ.x) X(x) ABST(λ.yy)
        ABST(λ.x) X(x) ABST(λ.yy)               # T(0)          # T(λ.x) X(x) ABST(λ.yy)
        T(λ.x) X(x) ABST(λ.yy)                  # V(1)          # T(λ.x) V(x) ABST(λ.yy)
        T(λ.x) V(x) ABST(λ.yy)                  # T(1)          # T(λ.x) T(x) ABST(λ.yy)
        T(λ.x) T(x) ABST(λ.yy)                  # APP(0:1)      # APP(λ.xx) ABST(λ.yy)
        APP(λ.xx) ABST(λ.yy)                    # T(0)          # T(λ.xx) ABST(λ.yy)
        T(λ.xx) ABST(λ.yy)                      # T(1)          # T(λ.xx) T(λ.yy)
        T(λ.xx) T(λ.yy)                         # APP(0:1)      # APP(λ.xxλ.yy)
        APP(λ.xxλ.yy)                           # T(0)          # T(λ.xxλ.yy)
    """.trimIndent()

    @Test
    fun test() {
        val g = simpleGrammar()
        val derivation = g.parseDerivation(input)
        assert(derivation.validate(g))
    }
}

sealed class DerivationValidationResult
object Ok: DerivationValidationResult()
data class UnexpectedNonTerm(val nt: NonTerminalSymbol): DerivationValidationResult()
data class UnexpectedNonTermRule(val nt: Rule): DerivationValidationResult()
data class LastProductionShoudHaveOneNt(val n: Int): DerivationValidationResult()
data class UnexpectedTerm(val t: TerminalSymbol): DerivationValidationResult()
data class UnexpectedTermRule(val t: Term): DerivationValidationResult()
data class UnexpectedRefRule(val ref: Ref): DerivationValidationResult()
data class BadChaining(val prev: List<Symbol>, val next: List<Symbol>): DerivationValidationResult()
data class BadStep(val left: List<Symbol>, val right: List<Symbol>): DerivationValidationResult()
data class BadProdReplacement(val rule: Prod, val symbols: List<Symbol>):DerivationValidationResult()
data class ImproperProdSymbol(val rule: Prod, val expected: Rule, val got: Rule): DerivationValidationResult()

class DerivationValidator(val g: Grammar) {
    fun validate(steps: List<DerivationStep>): DerivationValidationResult {
        val results = listOf(
            firstRuleIsTermOnly(steps.first()),
            lastRuleIsNonTerm(steps.last()),
            stepsAreChainedProperly(steps),
            checkIndividualSteps(steps)
        )

        TODO()
    }

    private fun checkIndividualSteps(steps: List<DerivationStep>): DerivationValidationResult {
        steps.map { s ->
            val (left, rule, range, right) = s
            listOf(
                checkLeftRightHasSameSymbols(left, right),
                checkReplacement(left, rule, range, right)
            )

        }
        TODO()
    }

    private fun checkReplacement(
        left: List<Symbol>,
        rule: Rule,
        range: IntRange,
        right: List<Symbol>
    ): DerivationValidationResult {
        val symbols = left.filterIndexed { index, _ ->  range.contains(index) }
        return when (rule) {
            is Prod -> {
                if (symbols.size != rule.args.size) {
                    BadProdReplacement(rule, symbols)
                }
                val symToRule = symbols.zip(rule.args.map(g::resolve))
                val notMatched = symToRule.firstOrNull {
                    it.first.rule != it.second
                }
                if (notMatched != null) {
                    ImproperProdSymbol(rule, notMatched.second, notMatched.first.rule)
                }
                TODO()
            }
            is Sum -> TODO()
            is Ref -> UnexpectedRefRule(rule)
            is Term -> UnexpectedTermRule(rule)
        }
    }

    private fun checkLeftRightHasSameSymbols(left: List<Symbol>, right: List<Symbol>): DerivationValidationResult {
        fun getSymbols(symbols: List<Symbol>): String {
            return symbols.joinToString("") {
                when (it) {
                    is NonTerminalSymbol -> it.text
                    is TerminalSymbol ->  it.char.toString()
                }
            }
        }

        if (getSymbols(left) != getSymbols(right)) {
            return BadStep(left, right)
        }
        return Ok
    }

    private fun stepsAreChainedProperly(steps: List<DerivationStep>): DerivationValidationResult {
        val notMatched = steps.zipWithNext { lhs, rhs ->
            lhs.result to rhs.input
        }.firstOrNull {  (lhs, rhs) ->
            lhs != rhs
        }
        if (notMatched != null) {
            return BadChaining(notMatched.first, notMatched.second)
        }
        return Ok
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

    private fun lastRuleIsNonTerm(s: DerivationStep): DerivationValidationResult {
        if (s.result.size != 1) {
            return LastProductionShoudHaveOneNt(s.result.size)
        }
        val lastSymbol = s.result.first()
        if (lastSymbol is TerminalSymbol) {
            return UnexpectedTerm(lastSymbol)
        }
        val lastRule = lastSymbol.rule
        if (lastRule is Term) {
            return UnexpectedTermRule(lastRule)
        }
        if (lastRule is Ref) {
            return UnexpectedRefRule(lastRule)
        }
        return Ok
    }
}
fun List<DerivationStep>.validate(g: Grammar): Boolean {

    TODO()
}