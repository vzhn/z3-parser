package me.vzhilin.gr

import me.vzhilin.gr.constraints.allConstraints
import me.vzhilin.gr.constraints.toExpressions
import me.vzhilin.gr.model.toDerivation
import me.vzhilin.gr.report.writeSvg
import me.vzhilin.gr.rules.DerivationStep
import me.vzhilin.gr.rules.Grammar
import me.vzhilin.gr.smt.SMTResult
import me.vzhilin.gr.smt.SMTRoutine
import java.io.File

sealed class SMTParsingResult {
    object NoSolutions: SMTParsingResult()
    object NotEnoughRows: SMTParsingResult()
    data class Solution(val derivation: List<DerivationStep>): SMTParsingResult()
}

class SMTParser(
    private val grammar: Grammar,
    private val input: String,
    private val rows: Int
) {
    private val columns = input.length

    fun parse(): SMTParsingResult {
        val constraints = allConstraints(grammar, rows, input)
        val exps = constraints.toExpressions(rows, columns)
        val smt = SMTRoutine(rows, columns, exps)
        return when (val rs = smt.solve()) {
            is SMTResult.Satisfiable -> {
                val cells = rs.cells
                writeSvg(File("report.svg"), input, grammar, cells)

                SMTParsingResult.Solution(cells.toDerivation(grammar))
            }
            SMTResult.Unknown -> SMTParsingResult.NoSolutions
            SMTResult.Unsat -> SMTParsingResult.NoSolutions
        }
    }
}