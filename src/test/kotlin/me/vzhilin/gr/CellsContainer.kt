package me.vzhilin.gr

import com.microsoft.z3.Context
import com.microsoft.z3.IntExpr

class CellsContainer(
        private val rows: Int,
        val columns: Int,
        ctx: Context)
{
    private val cells = 0 until rows * columns
    private val cellFields = mutableMapOf<Pair<Int, Z3Tests.Fields>, IntExpr>()

    private fun cellName(id: Int) = "cell($id)"
    fun constName(id: Int, field: Z3Tests.Fields) = "${cellName(id)}.${field.toString().lowercase()}"
    fun const(id: Int, field: Z3Tests.Fields): IntExpr {
        return cellFields[id to field]!!
    }

    init {
        cells.forEach { id ->
            Z3Tests.Fields.values().forEach { field ->
                val const = ctx.mkIntConst(constName(id, field))
                cellFields[id to field] = const
            }
        }
    }

    fun forEach(action: (cell: Cell) -> Unit) {
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                action(Cell(
                    id = row * columns + col,
                    row = row,
                    firstRow = row == 0,
                    lastRow = row == rows - 1,
                    firstColumn = col == 0,
                    lastColumn = col == columns - 1
                ))
            }
        }
    }

    fun <T> map(action: (Int) -> T): List<T> {
        return cells.map(action)
    }

    fun rule(id: Int) = const(id, Z3Tests.Fields.RULE)
    fun group(id: Int) = const(id, Z3Tests.Fields.GROUP)
    fun subgroup(id: Int) = const(id, Z3Tests.Fields.SUBGROUP)
    fun cell(id: Int) = const(id, Z3Tests.Fields.CELL)

    fun bottomId(id: Int): Int {
        if (id < this.columns)
            throw IllegalArgumentException()
        return id - this.columns
    }
}

data class Cell(
    val id: Int,
    val row: Int,
    val firstRow: Boolean,
    val lastRow: Boolean,
    val firstColumn: Boolean,
    val lastColumn: Boolean
)