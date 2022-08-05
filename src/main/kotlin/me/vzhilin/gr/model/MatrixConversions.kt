package me.vzhilin.gr.model

import me.vzhilin.gr.constraints.exp.ProductionTypeId
import me.vzhilin.gr.rules.*
import me.vzhilin.gr.smt.Cells

fun Cells.toDerivation(grammar: Grammar): List<DerivationStep> {
    TODO()
}

private fun Cells.validateGroups(rowId: Int, groups: Map<Int, List<Int>>, grammar: Grammar) {
    if (!groups.keys.increasing()) {
        throw IllegalArgumentException("rowId $rowId: groupIds should increase monotonically")
    }
    groups.forEach { (groupId, groupCells) ->
        val ruleIds = groupCells.map{ colId -> getRuleId(rowId, colId)}.distinct()
        if (ruleIds.size != 1) {
            throw IllegalArgumentException("rowId $rowId, groupId $groupId: different rowIds in same group: $ruleIds")
        }
        val ruleId = ruleIds.first()
        when (val rule = grammar[ruleId]) {
            is Prod -> {
                val prodSubGroups = groupCells.map{ colId -> getSubGroupId(rowId, colId) }
                if (!prodSubGroups.increasing()) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: Prod rule 'subGroupId' should increase monotonically")
                }

                if (prodSubGroups.size != rule.components.size) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: subGroupId.size != Prod.components.size")
                }

                if (groupCells.any { colId ->
                    getProductionTypeId(rowId, colId) != ProductionTypeId.PROD.n &&
                    getProductionTypeId(rowId, colId) != ProductionTypeId.BYPASS.n
                }) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: wrong 'prodTypeId'")
                }
            }
            is Sum -> {
                val sumSubGroups = groupCells.map { colId -> getSubGroupId(rowId, colId) }.distinct()
                if (sumSubGroups.size != 1 || sumSubGroups.first() != 0) {
                    throw IllegalArgumentException(
                        "rowId $rowId, groupId $groupId: Sum rule '${rule.name}' should have only one subGroup"
                    )
                }
                if (groupCells.any { colId ->
                        getProductionTypeId(rowId, colId) != ProductionTypeId.SUM.n &&
                        getProductionTypeId(rowId, colId) != ProductionTypeId.BYPASS.n
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

                if (getSubGroupId(rowId, groupCells.first()) != 0) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: Term rule should have 'subGroupId'=0")
                }
                if (groupCells.any { colId -> getProductionTypeId(rowId, colId) != ProductionTypeId.BYPASS.n }) {
                    throw IllegalArgumentException("rowId $rowId, groupId $groupId: wrong 'prodTypeId'")
                }
            }
        }
    }
}

private fun Collection<Int>.increasing(): Boolean {
    return isNotEmpty() && first() == 0 && zipWithNext().map { (a, b) -> b - a }.all { it == 1 }
}
