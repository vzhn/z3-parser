package me.vzhilin.gr.model

import me.vzhilin.gr.constraints.Config
import me.vzhilin.gr.constraints.Constraints
import me.vzhilin.gr.constraints.exp.And
import me.vzhilin.gr.constraints.exp.CellField
import me.vzhilin.gr.constraints.exp.ColumnId
import me.vzhilin.gr.constraints.exp.Const
import me.vzhilin.gr.constraints.exp.Eq
import me.vzhilin.gr.constraints.exp.Exp
import me.vzhilin.gr.constraints.exp.Ge
import me.vzhilin.gr.constraints.exp.GroupId
import me.vzhilin.gr.constraints.exp.Gt
import me.vzhilin.gr.constraints.exp.Iff
import me.vzhilin.gr.constraints.exp.Impl
import me.vzhilin.gr.constraints.exp.Inc
import me.vzhilin.gr.constraints.exp.Index
import me.vzhilin.gr.constraints.exp.Le
import me.vzhilin.gr.constraints.exp.Lt
import me.vzhilin.gr.constraints.exp.NatExp
import me.vzhilin.gr.constraints.exp.Neq
import me.vzhilin.gr.constraints.exp.Not
import me.vzhilin.gr.constraints.exp.One
import me.vzhilin.gr.constraints.exp.Or
import me.vzhilin.gr.constraints.exp.ProductionTypeId
import me.vzhilin.gr.constraints.exp.RowId
import me.vzhilin.gr.constraints.exp.RuleId
import me.vzhilin.gr.constraints.exp.SubGroupId
import me.vzhilin.gr.constraints.exp.Zero

import kotlin.reflect.KMutableProperty1

data class CellPosition(val row: Int, val col: Int) {
    override fun toString() = "Cell($row, $col)"
}

data class Matrix(
    val env: Config
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

    fun get(c: CellPosition): MatrixCell {
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
                is ProductionTypeId -> cell.prodTypeId
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
            is Iff -> ev(e.lhs) == ev(e.rhs)
            is Ge -> ev(e.lhs) >= ev(e.rhs)
            is Impl -> !ev(e.lhs) || ev(e.rhs)
            is Le -> ev(e.lhs) <= ev(e.rhs)
            is Neq -> ev(e.lhs) != ev(e.rhs)
            is Gt -> ev(e.lhs) > ev(e.rhs)
            is Lt -> ev(e.lhs) < ev(e.rhs)
            is Not -> !ev(e.lhs)
        }
        return res
    }

    fun getExpressions(cs: List<Constraints>): List<Exp> {
        val expressions = mutableListOf<Exp>()
        cs.forEach { c ->
            when (c) {
                is Constraints.Single -> {
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
                            val upper = CellPosition(rowId, colId)
                            val bottom = CellPosition(rowId - 1, colId)
                            expressions.add(c.handler(env, upper, bottom))
                        }
                    }
                }
                is Constraints.HorizontalPair -> {
                    for (rowId in 0 until rows) {
                        for (colId in 1 until columns) {
                            val right = CellPosition(rowId, colId)
                            val left = CellPosition(rowId, colId - 1)
                            expressions.add(c.handler(env, left, right))
                        }
                    }
                }
                is Constraints.Quad -> {
                    for (rowId in 1 until rows) {
                        for (colId in 1 until columns) {
                            val right = CellPosition(rowId, colId)
                            val left = CellPosition(rowId, colId - 1)
                            val rightBottom = CellPosition(rowId - 1, colId)
                            val leftBottom = CellPosition(rowId - 1, colId - 1)
                            expressions.add(c.handler(env,
                                left, right,
                                leftBottom, rightBottom
                            ))
                        }
                    }
                }
                is Constraints.Column -> {
                    for (colId in 0 until columns) {
                        val cells = (0 until rows).map { rowId -> CellPosition(rowId, colId) }
                        expressions.add(c.handler(env, colId, cells))
                    }
                }
                is Constraints.Row -> {
                    for (rowId in 0 until rows) {
                        val cells = (0 until columns).map { colId -> CellPosition(rowId, colId) }
                        expressions.add(c.handler(env, rowId, cells))
                    }
                }
            }
        }
        return expressions
    }
}

private fun prod(rows: Int, columns: Int): List<CellPosition> {
    val pairs = mutableListOf<CellPosition>()
    for (rowId in 0 until rows) {
        for (colId in 0 until columns) {
            pairs.add(CellPosition(rowId, colId))
        }
    }
    return pairs
}
