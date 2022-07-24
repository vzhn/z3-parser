package me.vzhilin.gr

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.BoolSort
import com.microsoft.z3.Context
import com.microsoft.z3.Expr
import com.microsoft.z3.IntExpr
import kotlin.test.Test

class CellsContainer(numberOfCells: Int, ctx: Context) {
    private val cells = 0 until numberOfCells
    private val cellFields = mutableMapOf<Pair<Int, Z3Tests.Fields>, IntExpr>()

    fun cell(id: Int) = "cell($id)"
    fun constName(id: Int, field: Z3Tests.Fields) = "${cell(id)}.${field.toString().lowercase()}"
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

    fun forEach(action: (Int) -> Unit) {
        cells.forEach(action)
    }

    fun forEachAdjacentPair(action: (Int, Int) -> Unit) {
        cells.forEachAdjacentPair(action)
    }
}

class CellAssertions(private val cellsContainer: CellsContainer) {
    fun makeFirstCell(ctx: Context): List<BoolExpr> {
        val zero = ctx.mkInt(0)
        return listOf(
            ctx.mkEq(cellsContainer.const(0, Z3Tests.Fields.LINE), zero),
            ctx.mkEq(cellsContainer.const(0, Z3Tests.Fields.ROW), zero),
            ctx.mkEq(cellsContainer.const(0, Z3Tests.Fields.GROUP), zero),
            ctx.mkEq(cellsContainer.const(0, Z3Tests.Fields.CELL), zero)
        )
    }

    fun make(ctx: Context): List<Expr<BoolSort>> {
        return makeFirstCell(ctx) + makeAdjacentCells(ctx)
    }

    fun makeAdjacentPair(ctx: Context, left: Int, right: Int): BoolExpr {
        val one = ctx.mkInt(1)
        val zero = ctx.mkInt(0)
        fun inc(ex: IntExpr) = ctx.mkAdd(one, ex)
        fun const(id: Int, field: Z3Tests.Fields) = cellsContainer.const(id, field)

        fun eq(f: Z3Tests.Fields) = ctx.mkEq(const(right, f), const(left, f))
        fun eqNext(f: Z3Tests.Fields) = ctx.mkEq(const(right, f), inc(const(left, f)))
        fun zero(f: Z3Tests.Fields) = ctx.mkEq(const(right, f), zero)

        return ctx.mkOr(
            ctx.mkAnd(
                eqNext(Z3Tests.Fields.LINE),
                zero(Z3Tests.Fields.ROW),
                zero(Z3Tests.Fields.GROUP),
                zero(Z3Tests.Fields.CELL)
            ),
            ctx.mkAnd(
                eq(Z3Tests.Fields.LINE),
                ctx.mkOr(
                    ctx.mkAnd(
                        eqNext(Z3Tests.Fields.ROW),
                        zero(Z3Tests.Fields.GROUP),
                        zero(Z3Tests.Fields.CELL)
                    ),
                    ctx.mkAnd(
                        eq(Z3Tests.Fields.ROW),
                        ctx.mkOr(
                            ctx.mkAnd(
                                eq(Z3Tests.Fields.GROUP),
                                eqNext(Z3Tests.Fields.CELL)
                            ),
                            ctx.mkAnd(
                                eqNext(Z3Tests.Fields.GROUP),
                                zero(Z3Tests.Fields.CELL)
                            )
                        )
                    )
                )
            )
        )
    }

    fun makeAdjacentCells(ctx: Context): List<BoolExpr> {
        val rs = mutableListOf<BoolExpr>()
        cellsContainer.forEachAdjacentPair { left, right ->
            rs.add(makeAdjacentPair(ctx, left, right))
        }
        return rs
    }
}

class GrammarAssertions(private val cells: CellsContainer) {
    fun make(ctx: Context): List<BoolExpr> {
        val rs = mutableListOf<BoolExpr>()
        cells.forEachAdjacentPair { left, right ->
            rs.add(
                ctx.mkImplies(
                    ctx.mkEq(
                        cells.const(right, Z3Tests.Fields.GROUP),
                        cells.const(left, Z3Tests.Fields.GROUP)
                    ),
                    ctx.mkEq(
                        cells.const(right, Z3Tests.Fields.RULE),
                        cells.const(left, Z3Tests.Fields.RULE)
                    )
                )
            )
        }

        return rs
    }
}

class SmtParser {
    private val numberOfCells = 2
    private val ctx = Context()
    private val cells = CellsContainer(numberOfCells, ctx)
}

class Z3Tests {
    enum class Fields { LINE, ROW, GROUP, RULE, CELL }

    @Test
    fun test() {
        val numberOfCells = 2
        solve(numberOfCells)
    }

    fun solve(numberOfCells: Int) {
        val ctx = Context()
        val solver = ctx.mkSolver()

        val cells = CellsContainer(numberOfCells, ctx)
        val cellAssertions = CellAssertions(cells)
        solver.add(*cellAssertions.make(ctx).toTypedArray())

        val grammarAssertions = GrammarAssertions(cells)
        solver.add(*grammarAssertions.make(ctx).toTypedArray())
        solver.check()



//        cells.forEach { id ->
//            Fields.values().forEach { field ->
//                val const = const(id, field)
//                val value = (solver.model.getConstInterp(const) as IntNum).int
//                println("${constName(id, field)} = $value")
//            }
//        }
    }
}