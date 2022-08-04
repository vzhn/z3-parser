package me.vzhilin.gr

import me.vzhilin.gr.constraints.Config
import me.vzhilin.gr.constraints.allConstraints
import me.vzhilin.gr.constraints.toExpressions
import me.vzhilin.gr.model.toDerivation
import me.vzhilin.gr.rules.DerivationStep
import me.vzhilin.gr.rules.Grammar
import me.vzhilin.gr.smt.SMTResult
import me.vzhilin.gr.smt.SMTRoutine

sealed class SMTParserResult {
    object NoSolutions: SMTParserResult()
    object NotEnoughRows: SMTParserResult()
    data class Solution(val derivation: List<DerivationStep>): SMTParserResult()
}

class SMTParser(
    private val grammar: Grammar,
    input: String,
    private val rows: Int
) {
    private val columns = input.length

    fun parse(): SMTParserResult {
        val config = Config(grammar, rows, columns)
        val constraints = config.allConstraints(rows, columns)
        val exps = constraints.toExpressions(rows, columns)
        val smt = SMTRoutine(rows, columns, exps)
        return when (val rs = smt.solve()) {
            is SMTResult.Satisfiable -> SMTParserResult.Solution(rs.cells.toDerivation(grammar))
            SMTResult.UNKNOWN -> SMTParserResult.NoSolutions
            SMTResult.UNSAT -> SMTParserResult.NoSolutions
        }
    }
}