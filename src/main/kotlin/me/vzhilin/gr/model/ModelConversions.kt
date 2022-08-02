package me.vzhilin.gr.model

import me.vzhilin.gr.constraints.exp.ProductionTypeId
import me.vzhilin.gr.rules.*

fun Matrix.toDerivation(): List<DerivationStep> {
    // TODO validate first row
    val firstRow = get(0)
    val inputString = firstRow.map { grammar[it.ruleId] as Term }.map { it.ch }.joinToString("")

    (0 until env.rows).map { rowId ->
        val row = get(rowId)
        validateRow(row)
        val groups = row.groupBy { it.groupId }
        if (groups.keys.min() != 0) {
            throw IllegalArgumentException("rowId: $rowId, groupId should start from 0, got: ${groups.keys.min()}")
        }
        if (groups.keys.zipWithNext().map { (a, b) -> b - a }.all { it == 1 }) {
            throw IllegalArgumentException("rowId: $rowId, groupIds should increase monotonically")
        }

        val productionRules = groups.filterValues { it -> it.any { it.prodTypeId != ProductionTypeId.BYPASS.n } }
        val symbols = groups.map { (_, groupedCells) ->
            val groupRuleId = groupedCells.first().ruleId
            val symbolRule = grammar[groupRuleId]
            val word = groupedCells.map { inputString[it.columnId] }.joinToString("")
            when (symbolRule) {
                is Prod, is Sum -> NonTerminalDerivation(symbolRule, word)
                is Term -> TerminalDerivation(symbolRule)
            }
        }

        if (rowId == env.rows - 1) {
            if (productionRules.isNotEmpty()) {
                throw IllegalArgumentException("rowId: $rowId: expected no productions in last rule")
            }
            DerivationStep.Tail(symbols)
        } else {
            if (productionRules.size != 1) {
                throw IllegalArgumentException("rowId: $rowId: expected one production rule per row, got: ${productionRules.size}")
            }
            val productionRule = productionRules.values.first()
            val productionRuleTypes = productionRule.map(MatrixCell::prodTypeId).distinct()
            if (productionRuleTypes.size != 1) {
                throw IllegalArgumentException("rowId: $rowId: having different 'prodTypeId' in same group")
            }

            // fixme move to row sanity checks
            val ruleIds = productionRule.map(MatrixCell::ruleId).distinct()
            if (ruleIds.size != 1) {
                throw IllegalArgumentException("rowId: $rowId: having different 'ruleId' in same group")
            }

            val rule = grammar[ruleIds.first()]
            val range = productionRule.first().columnId..productionRule.last().columnId
            DerivationStep.Middle(symbols, rule, range)
        }
    }
    TODO()
}

private fun Matrix.validateRow(rowId: Int, row: List<MatrixCell>) {
    if (row.size != env.columns) {
        throw IllegalArgumentException("rowId $rowId: expected number of columns: '${env.columns}', got '${row.size}'")
    }
    if (row.filterIndexed { index, cell -> cell.columnId != index }.isNotEmpty()) {
        throw IllegalArgumentException("rowId $rowId: 'columnId' does not corresponds column ids")
    }
    if (row.first().columnId != 0) {
        throw IllegalArgumentException("rowId $rowId: expected first 'columnId' is 0, got: '${row.first().columnId}'")
    }
    if (row.map(MatrixCell::columnId).zipWithNext().any { (a, b) -> b - a != 1 }) {
        throw IllegalArgumentException("rowId $rowId: 'columnId' does not increase monotonically")
    }
    TODO("Not yet implemented")
}
