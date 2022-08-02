package me.vzhilin.gr

import me.vzhilin.gr.constraints.Config
import me.vzhilin.gr.constraints.allConstraints
import me.vzhilin.gr.model.Matrix
import me.vzhilin.gr.rules.DerivationStep
import me.vzhilin.gr.rules.Grammar
import me.vzhilin.gr.smt.SMTRoutine

class SMTParser(
    private val grammar: Grammar,
    private val input: String,
    private val rows: Int
) {
    private val columns = input.length

    fun parse(): List<DerivationStep> {
        val config = Config(grammar, rows, columns)
        val constraints = config.allConstraints()
        val m = Matrix(grammar, rows, columns)
        val exps = m.getExpressions(constraints)
        val smt = SMTRoutine(rows, columns, exps)
        TODO()
    }
}