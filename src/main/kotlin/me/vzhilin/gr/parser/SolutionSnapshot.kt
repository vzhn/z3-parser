package me.vzhilin.gr.parser

import me.vzhilin.gr.parser.smt.Cells

data class SolutionSnapshot(
    val data: Map<Pair<Int, Int>, SolutionSnapshotCell>
) {
    companion object {
        fun of(cells: Cells): SolutionSnapshot {
            val result = mutableMapOf<Pair<Int, Int>, SolutionSnapshotCell>()
            for (rowId in 0 until cells.rows) {
                for (colId in 0 until cells.cols) {
                    val groupId = cells.getGroupId(rowId, colId)
                    val symbolId = cells.getSymbolId(rowId, colId)
                    result[rowId to colId] = SolutionSnapshotCell(groupId, symbolId)
                }
            }
            return SolutionSnapshot(result)
        }
    }
}

data class SolutionSnapshotCell(
    val groupId: Int,
    val symbolId: Int
)

fun List<SolutionSnapshot>.toExpression(): Exp {
    val snapshotsExpressions = this.map { solutionSnapshot ->
        val expressions = solutionSnapshot.data.flatMap { (pair, cell) ->
            val (rowId, colId) = pair
            listOf(
                GroupId(rowId, colId) eq Const(cell.groupId),
                SymbolId(rowId, colId) eq Const(cell.symbolId)
            )
        }
        Not(And(expressions))
    }
    return And(snapshotsExpressions)
}