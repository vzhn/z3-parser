package me.vzhilin.gr.snapshot

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