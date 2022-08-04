package me.vzhilin.gr

import me.vzhilin.gr.constraints.Config
import me.vzhilin.gr.constraints.allConstraints
import me.vzhilin.gr.constraints.toExpressions
import me.vzhilin.gr.model.toDerivation
import me.vzhilin.gr.rules.DerivationStep
import me.vzhilin.gr.rules.Grammar
import me.vzhilin.gr.smt.Cells
import me.vzhilin.gr.smt.SMTResult
import me.vzhilin.gr.smt.SMTRoutine

class SMTParser(
    private val grammar: Grammar,
    input: String,
    private val rows: Int
) {
    private val columns = input.length

    fun parse(): List<DerivationStep> {
        val config = Config(grammar, rows, columns)
        val constraints = config.allConstraints(rows, columns)
        val exps = constraints.toExpressions(rows, columns)
        val smt = SMTRoutine(rows, columns, exps)
        when (val rs = smt.solve()) {
            is SMTResult.Satisfiable -> {
                return rs.cells.toDerivation(grammar)
            }
            SMTResult.UNKNOWN -> TODO()
            SMTResult.UNSAT -> TODO()
        }
    }
}