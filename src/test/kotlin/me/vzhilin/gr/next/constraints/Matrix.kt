package me.vzhilin.gr.next.constraints

import me.vzhilin.gr.next.*
import kotlin.reflect.KMutableProperty1

data class Matrix(
    val env: Environment
) {
    private val columns: Int = env.columns
    private val rows: Int = env.rows

    // (col, row)
    private val data: MutableList<MutableList<MatrixCell>> = mutableListOf()

    init {
        (0 until rows).forEach { rowId ->
            val row = mutableListOf<MatrixCell>()
            data.add(row)

            (0 until columns).forEach { colId ->
                row.add(MatrixCell(rowId, colId))
            }
        }
    }

    operator fun get(rowId: Int) = data[rowId]
    fun set(f: KMutableProperty1<MatrixCell, Int>, vararg vs: Int) {
        vs.forEachIndexed { index, v ->
            val rowId = (rows - 1) - (index / columns)
            val colId = index % columns
            f.set(data[rowId][colId], v)
        }
    }

    fun validate(vararg constraints: Constraints): Boolean {
        val expressions = getExpressions(constraints.toList())
        val failedExp = expressions.firstOrNull { !ev(it) }
        return failedExp == null
    }

    fun get(c: Cell): MatrixCell {
        return get(c.row)[c.col]
    }

    fun ev(exp: NatExp): Int = when (exp) {
        is CellField -> {
            val cell = get(exp.cell)
            when (exp) {
                is ColumnId -> cell.columnId
                is GroupId -> cell.groupId
                is Index -> cell.index
                is RowId -> cell.rowId
                is RuleId -> cell.ruleId
                is RuleTypeId -> cell.ruleTypeId
                is SubGroupId -> cell.subGroupId
            }
        }

        is Const -> exp.n
        is Inc -> ev(exp.n) + 1
        One -> 1
        Zero -> 0
    }

    fun ev(e: Exp): Boolean {
        val res = when (e) {
            is Or -> e.exps.any(this::ev)
            is And -> e.exps.all(this::ev)
            is Eq -> ev(e.lhs) == ev(e.rhs)
            is Ge -> ev(e.lhs) >= ev(e.rhs)
            is Impl -> !ev(e.lhs) || ev(e.rhs)
            is Le -> ev(e.lhs) <= ev(e.rhs)
            is Neq -> ev(e.lhs) != ev(e.rhs)
        }
        return res
    }

    fun getExpressions(cs: List<Constraints>): List<Exp> {
        val expressions = mutableListOf<Exp>()
        cs.forEach { c ->
            when (c) {
                is Constraints.Cell -> {
                    prod(rows, columns).map { cell ->
                        c.handler(env, cell)
                    }.toCollection(expressions)
                }
                is Constraints.FirstColumn -> {
                    prod(rows, 1).map { cell ->
                        c.handler(env, cell)
                    }.toCollection(expressions)
                }
                is Constraints.VerticalPair -> {
                    for (rowId in 1 until rows) {
                        for (colId in 0 until columns) {
                            val upper = Cell(rowId, colId)
                            val bottom = Cell(rowId - 1, colId)
                            expressions.add(c.handler(env, upper, bottom))
                        }
                    }
                }
                is Constraints.HorizontalPair -> {
                    for (rowId in 0 until rows) {
                        for (colId in 1 until columns) {
                            val right = Cell(rowId, colId)
                            val left = Cell(rowId, colId - 1)
                            expressions.add(c.handler(env, left, right))
                        }
                    }
                }
                is Constraints.Column -> {
                    for (colId in 0 until columns) {
                        val cells = (0 until rows).map { rowId -> Cell(rowId, colId) }
                        expressions.add(c.handler(env, colId, cells))
                    }
                }
                is Constraints.Row -> {
                    for (rowId in 0 until rows) {
                        val cells = (0 until columns).map { colId -> Cell(rowId, colId) }
                        expressions.add(c.handler(env, rowId, cells))
                    }
                }
            }
        }
        return expressions
    }
}