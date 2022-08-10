package me.vzhilin.gr

import me.vzhilin.gr.parser.allConstraints
import me.vzhilin.gr.parser.toExpressions
import me.vzhilin.gr.model.toDerivation
import me.vzhilin.gr.parser.ComputeLimits
import me.vzhilin.gr.parser.DerivationLimits
import me.vzhilin.gr.parser.DerivationStep
import me.vzhilin.gr.parser.DerivationSymbol
import me.vzhilin.gr.parser.Grammar
import me.vzhilin.gr.parser.NonTerm
import me.vzhilin.gr.parser.NonTerminalDerivation
import me.vzhilin.gr.parser.TerminalDerivation
import me.vzhilin.gr.parser.Cells
import me.vzhilin.gr.parser.SMTResult
import me.vzhilin.gr.parser.SMTRoutine
import me.vzhilin.gr.parser.SolutionSnapshot
import me.vzhilin.gr.parser.toExpression

sealed class SMTParsingResult {
    object NoSolutions: SMTParsingResult()
    data class Solution(
        val input: String,
        val grammar: Grammar,
        val cells: Cells,
        val derivation: List<DerivationStep>
    ): SMTParsingResult() {
        fun printCells() {
            val revRows = cells.rs.reversed()
            fun values(rowId: Int, f: (rowId: Int, colId: Int) -> Int): String {
                return cells.cs.joinToString(", ") { colId -> f(rowId, colId).toString() }
            }

            fun valuesStr(rowId: Int, f: (colId: Int) -> String): String {
                return cells.cs.joinToString(", ") { colId -> "\"${f(colId)}\"" }
            }

            println("val input = \"$input\"")
            println("with(Cells(${cells.rows}, ${cells.cols})) {")
            for (rowId in revRows) {
                println("\tsetRuleId($rowId, grammar, ${valuesStr(rowId) { colId -> cells.getRule(grammar, rowId, colId).name }})")
            }
            for (rowId in revRows) { println("\tsetGroupId($rowId, ${values(rowId, cells::getGroupId)})") }
            for (rowId in revRows) { println("\tsetSubGroupId($rowId, ${values(rowId, cells::getSubGroupId)})") }
            for (rowId in revRows) { println("\tsetProdTypeId($rowId, ${values(rowId, cells::setProdTypeId)})") }
            for (rowId in revRows) { println("\tsetIndex($rowId, ${values(rowId, cells::getIndex)})") }
            println("}")
        }

        fun printDerivation() {
            fun asString(list: List<DerivationSymbol>): String {
                return list.joinToString(" ") { sym ->
                    when (sym) {
                        is NonTerminalDerivation -> "${sym.rule.name}(\'${sym.word}\')"
                        is TerminalDerivation -> "'${sym.rule.ch}'"
                    }
                }.replace("' '", "")
            }

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
            println(lines.joinToString("\n"))
        }
    }
}

class SMTParser(
    private val grammar: Grammar,
    private val input: String,
    private val goal: NonTerm?
) {
    private val limitsComputer = ComputeLimits(grammar)
    private var debug: Boolean = false
    private val columns = input.length
    private val solutionSnapshots = mutableListOf<SolutionSnapshot>()
    private val limits = if (goal != null) {
        limitsComputer.computeTreeHeights(goal, input.length)
    } else {
        (grammar.sums + grammar.prods).map { goal ->
            limitsComputer.computeTreeHeights(goal, input.length)
        }.reduce { left, right ->
            DerivationLimits(
                minOf(left.min, right.min),
                maxOf(left.max, right.max)
            )
        }
    }

    private var rows = limits.min

    fun parse(): SMTParsingResult {
        while (rows <= limits.max) {
            val constraints = allConstraints(grammar, rows, input, goal)
            val exps = constraints.toExpressions(rows, columns) + solutionSnapshots.toExpression()
            val smt = SMTRoutine(rows, columns, exps)

            when (val rs = smt.solve()) {
                is SMTResult.Satisfiable -> {
                    val cells = rs.cells
                    solutionSnapshots.add(SolutionSnapshot.of(cells))
                    return SMTParsingResult.Solution(input, grammar, cells, cells.toDerivation(grammar))
                }
                SMTResult.Unknown, SMTResult.Unsat -> ++rows
            }
        }

        return SMTParsingResult.NoSolutions
    }
}
