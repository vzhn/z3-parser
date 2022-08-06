package me.vzhilin.gr

import me.vzhilin.gr.constraints.allConstraints
import me.vzhilin.gr.constraints.toExpressions
import me.vzhilin.gr.model.toDerivation
import me.vzhilin.gr.rules.*
import me.vzhilin.gr.smt.SMTResult
import me.vzhilin.gr.smt.SMTRoutine
import me.vzhilin.gr.snapshot.SolutionSnapshot
import me.vzhilin.gr.snapshot.toExpression

sealed class SMTParsingResult {
    object NoSolutions: SMTParsingResult()
    object NotEnoughRows: SMTParsingResult()
    data class Solution(val derivation: List<DerivationStep>): SMTParsingResult()
}

class SMTParser(
    private val grammar: Grammar,
    private val input: String,
    private val rows: Int,
    private val goal: NonTerm?
) {
    private val columns = input.length
    private val solutionSnapshots = mutableListOf<SolutionSnapshot>()

    fun parse(): SMTParsingResult {
        val constraints = allConstraints(grammar, rows, input, goal)
        val exps = constraints.toExpressions(rows, columns) + solutionSnapshots.toExpression()
        val smt = SMTRoutine(rows, columns, exps)
        return when (val rs = smt.solve()) {
            is SMTResult.Satisfiable -> {
                val cells = rs.cells
                solutionSnapshots.add(SolutionSnapshot.of(cells))
                SMTParsingResult.Solution(cells.toDerivation(grammar))
            }
            SMTResult.Unknown -> SMTParsingResult.NoSolutions
            SMTResult.Unsat -> SMTParsingResult.NoSolutions
        }
    }
}

fun SMTParsingResult.print() {
    fun asString(list: List<DerivationSymbol>): String {
        return list.joinToString(" ") { sym ->
            when (sym) {
                is NonTerminalDerivation -> "${sym.rule.name}(\'${sym.word}\')"
                is TerminalDerivation -> "'${sym.rule.ch}'"
            }
        }.replace("' '", "")
    }

    val output = when (this) {
        SMTParsingResult.NoSolutions -> "no solutions"
        SMTParsingResult.NotEnoughRows -> "not enough rows"
        is SMTParsingResult.Solution -> {
            val leftColumn = mutableListOf<String>()
            val rightColumn = mutableListOf<String>()
            var tail = ""

            this.derivation.forEach { step -> when (step) {
                is DerivationStep.Middle -> {
                    val substitutions = step.substitutions.joinToString(" ") {
                        val name = it.first.name

                        val (first, last) = it.second.first to it.second.last
                        val range = if (first != last) {
                            "${first}:${last}"
                        } else {
                            "$first"
                        }
                        "${name}($range)"
                    }

                    leftColumn.add(asString(step.input))
                    rightColumn.add(substitutions)
                }
                is DerivationStep.Tail -> {
                    tail = asString(step.input)
                }
            } }

            val maxLength = leftColumn.maxOf(String::length)
            val lines = leftColumn.zip(rightColumn).map { (left, right) ->
                "$left ${" ".repeat(maxLength - left.length)} # $right"
            } + tail

            lines.joinToString("\n")
        }
    }

    println(output)
}
