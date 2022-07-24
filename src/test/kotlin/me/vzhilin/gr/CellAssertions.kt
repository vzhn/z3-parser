package me.vzhilin.gr

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.BoolSort
import com.microsoft.z3.Context
import com.microsoft.z3.Expr

class CellAssertions(private val cs: CellsContainer) {
    fun make(ctx: Context): List<Expr<BoolSort>> {
        val one = ctx.mkInt(1)
        val zero = ctx.mkInt(0)
        val rs = mutableListOf<BoolExpr>()

        cs.forEach { cell ->
            fun makeFirstCell() {
                rs.add(ctx.mkEq(cs.cell(cell.id), zero))
                rs.add(ctx.mkEq(cs.group(cell.id), zero))
                rs.add(ctx.mkEq(cs.subgroup(cell.id), zero))
            }
            fun makeAdjacentCells() {
                val prev = cell.id - 1
                rs.add(
                    ctx.mkOr(
                        ctx.mkAnd(
                            ctx.mkEq(cs.group(prev), cs.group(cell.id)),
                            ctx.mkEq(cs.cell(cell.id), ctx.mkAdd(one, cs.cell(prev))),
                            ctx.mkOr(
                                ctx.mkEq(cs.subgroup(cell.id), cs.subgroup(prev)),
                                ctx.mkEq(cs.subgroup(cell.id), ctx.mkAdd(ctx.mkInt(1), cs.subgroup(prev)))
                            )
                        ),
                        ctx.mkAnd(
                            ctx.mkEq(cs.group(cell.id), ctx.mkAdd(one, cs.group(prev))),
                            ctx.mkEq(cs.cell(cell.id), zero),
                            ctx.mkEq(cs.subgroup(cell.id), zero)
                        )
                    )
                )
            }
            fun makeGroups() {
                val prev = cell.id - 1
                val bottom = cs.bottomId(cell.id)
                val bottomPrev = cs.bottomId(prev)

                rs.add(
                    ctx.mkImplies(
                        ctx.mkNot(ctx.mkEq(cs.group(prev), cs.group(cell.id))),
                        ctx.mkNot(ctx.mkEq(cs.group(bottomPrev), cs.group(bottom)))
                    )
                )
            }

            if (cell.firstColumn) {
                makeFirstCell()
            } else {
                makeAdjacentCells()
                if (!cell.firstRow) {
                    makeGroups()
                }
            }
        }
        return rs
    }

}