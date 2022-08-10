package me.vzhilin.gr.snapshot

import me.vzhilin.gr.parser.exp.*
import me.vzhilin.gr.smt.Cells

data class SolutionSnapshot(
    val data: Map<Pair<Int, Int>, SolutionSnapshotCell>
) {
    companion object {
        fun of(cells: Cells): SolutionSnapshot {
            val result = mutableMapOf<Pair<Int, Int>, SolutionSnapshotCell>()
            for (rowId in 0 until cells.rows) {
                for (colId in 0 until cells.cols) {
                    val groupId = cells.getGroupId(rowId, colId)
                    val ruleId = cells.getRuleId(rowId, colId)
                    result[rowId to colId] = SolutionSnapshotCell(groupId, ruleId)
                }
            }
            return SolutionSnapshot(result)
        }
    }
}

data class SolutionSnapshotCell(
    val groupId: Int,
    val ruleId: Int
)

fun List<SolutionSnapshot>.toExpression(): Exp {
    val snapshotsExpressions = this.map { solutionSnapshot ->
        val expressions = solutionSnapshot.data.flatMap { (pair, cell) ->
            val (rowId, colId) = pair
            listOf(
                GroupId(rowId, colId) eq Const(cell.groupId),
                RuleId(rowId, colId) eq Const(cell.ruleId)
            )
        }
        Not(And(expressions))
    }
    return And(snapshotsExpressions)
}