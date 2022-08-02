package me.vzhilin.gr.model

import me.vzhilin.gr.constraints.exp.ProductionTypeId
import me.vzhilin.gr.rules.*

fun Matrix.toDerivation(): List<DerivationStep> {
    // TODO validate first row
    val firstRow = get(0)
    val inputString = firstRow.map { grammar[it.ruleId] as Term }.map { it.ch }.joinToString("")

    return (0 until env.rows).map { rowId ->
        val row = get(rowId)
        validateRow(rowId, row)

        val groups = row.groupBy { it.groupId }
        validateGroups(rowId, groups)

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
}

private fun Matrix.validateRow(rowId: Int, row: List<MatrixCell>) {
    if (row.any { it.rowId != rowId }) {
        throw IllegalArgumentException("rowId $rowId: cell 'rowId' does not correspond actual rowId")
    }
    if (row.size != env.columns) {
        throw IllegalArgumentException("rowId $rowId: expected number of columns: '${env.columns}', got '${row.size}'")
    }
    if (row.filterIndexed { index, cell -> cell.columnId != index }.isNotEmpty()) {
        throw IllegalArgumentException("rowId $rowId: 'columnId' does not corresponds column ids")
    }
    if (row.first().columnId != 0) {
        throw IllegalArgumentException("rowId $rowId: expected first 'columnId' is 0, got: '${row.first().columnId}'")
    }
    if (!row.map(MatrixCell::columnId).increasing()) {
        throw IllegalArgumentException("rowId $rowId: 'columnId' does not increase monotonically")
    }
}

private fun Matrix.validateGroups(rowId: Int, groups: Map<Int, List<MatrixCell>>) {
    if (!groups.keys.increasing()) {
        throw IllegalArgumentException("rowId $rowId: groupIds should increase monotonically")
    }
    groups.forEach { (groupId, groupCells) ->
        val ruleIds = groupCells.map(MatrixCell::ruleId).distinct()
        if (ruleIds.size != 1) {
            throw IllegalArgumentException("rowId $rowId, groupId $groupId: different rowIds in same group: $ruleIds")
        }
        val ruleId = ruleIds.first()
        when (val rule = grammar[ruleId]) {
            is Prod -> {
                val prodSubGroups = groupCells.map(MatrixCell::subGroupId)
                if (!prodSubGroups.increasing()) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: Prod rule 'subGroupId' should increase monotonically")
                }

                if (prodSubGroups.size != rule.components.size) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: subGroupId.size != Prod.components.size")
                }

                if (groupCells.any {
                    it.prodTypeId != ProductionTypeId.PROD.n &&
                    it.prodTypeId != ProductionTypeId.BYPASS.n
                }) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: wrong 'prodTypeId'")
                }
            }
            is Sum -> {
                val sumSubGroups = groupCells.map { it.subGroupId }.distinct()
                if (sumSubGroups.size != 1 || sumSubGroups.first() != 0) {
                    throw IllegalArgumentException(
                        "rowId $rowId, groupId $groupId: Sum rule '${rule.name}' should have only one subGroup"
                    )
                }
                if (groupCells.any {
                    it.prodTypeId != ProductionTypeId.SUM.n &&
                    it.prodTypeId != ProductionTypeId.BYPASS.n
                }) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: wrong 'prodTypeId'")
                }
            }
            is Term -> {
                if (groupCells.size > 1) {
                    throw IllegalArgumentException(
                        "rowId $rowId, groupId $groupId: Term rule '${rule.name}' should have only one symbol"
                    )
                }

                if (groupCells.first().subGroupId != 0) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: Term rule should have 'subGroupId'=0")
                }
                if (groupCells.any { it.prodTypeId != ProductionTypeId.BYPASS.n }) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: wrong 'prodTypeId'")
                }
            }
        }
    }
}

private fun Collection<Int>.increasing(): Boolean {
    return isNotEmpty() && first() == 0 && zipWithNext().map { (a, b) -> b - a }.all { it == 1 }
}
