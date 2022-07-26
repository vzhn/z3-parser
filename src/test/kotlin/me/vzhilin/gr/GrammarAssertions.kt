package me.vzhilin.gr

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context

class GrammarAssertions(
    private val g: Grammar,
    private val cs: CellsContainer
) {
    private fun matchRule(
        ctx: Context,
        id: Int,
        r: Rule
    ) = ctx.mkEq(cs.rule(id), ctx.mkInt(g.id(r)))

    fun make(ctx: Context): List<BoolExpr> {
        val rs = mutableListOf<BoolExpr>()
        cs.forEach { cell ->
            val bottomId by lazy { cs.bottomId(cell.id) }

            fun ruleConstraint(): BoolExpr {
                return ctx.mkAnd(
                    ctx.mkGe(cs.rule(cell.id), ctx.mkInt(0)),
                    ctx.mkLe(cs.rule(cell.id), ctx.mkInt(g.allRules.lastIndex))
                )
            }
            fun firstRow(): List<BoolExpr> {
                val exps = g.terms.map { matchRule(ctx, cell.id, it) }.toTypedArray()
                return listOf(ctx.mkOr(*exps))
            }
            fun termConstraints(): List<BoolExpr> {
                val ts = g.terms.map {
                    ctx.mkEq(cs.rule(cell.id), ctx.mkInt(g.id(it)))
                }.toTypedArray()
                return listOf(ctx.mkOr(*ts))
            }
            fun sumConstraints(): List<BoolExpr> {
                return g.sums.map { sum ->
                    val exps = sum.args.map(g::resolve).map {
                        ctx.mkEq(cs.rule(bottomId), ctx.mkInt(g.id(it)))
                    }.toTypedArray()

                    ctx.mkImplies(
                        matchRule(ctx, cell.id, sum),
                        ctx.mkAnd(
                            ctx.mkOr(*exps),
                            ctx.mkEq(cs.cell(bottomId), cs.cell(cell.id))
                        )
                    )
                }
            }
            fun productConstraints(): List<BoolExpr> {
                val rs = mutableListOf<BoolExpr>()
                g.prods.map { prod ->
                    fun firstCell() {
                        val args = prod.args.map(g::resolve)
                        rs.add(ctx.mkImplies(
                            ctx.mkAnd(
                                matchRule(ctx, cell.id, prod),
                                ctx.mkEq(cs.cell(cell.id), ctx.mkInt(0))
                            ),
                            ctx.mkAnd(
                                ctx.mkEq(cs.rule(bottomId), ctx.mkInt(g.id(args.first()))),
                                ctx.mkEq(cs.cell(bottomId), ctx.mkInt(0)),
                                ctx.mkEq(cs.subgroup(cell.id), ctx.mkInt(0))
                            )
                        ))
                    }

                    val last = g.resolve(prod.args.last())
                    fun adjCells() {
                        val bottomPrev = bottomId - 1
                        val prev = cell.id - 1

                        val subGroups = prod.args.map(g::resolve).zipWithNext().mapIndexed { index, (a, b) ->
                            ctx.mkAnd(
                                ctx.mkEq(cs.subgroup(prev), ctx.mkInt(index)),
                                ctx.mkEq(cs.rule(bottomPrev), ctx.mkInt(g.id(a))),
                                ctx.mkEq(cs.rule(prev), ctx.mkInt(g.id(b)))
                            )
                        }.toTypedArray()

                        rs.add(ctx.mkImplies(
                            ctx.mkAnd(
                                matchRule(ctx, cell.id, prod),
                                matchRule(ctx, prev, prod)
                            ),
                            ctx.mkAnd(
                                ctx.mkIff(
                                    ctx.mkEq(cs.group(bottomPrev), cs.group(bottomId)),
                                    ctx.mkEq(cs.subgroup(prev), cs.group(cell.id))
                                ),
                                ctx.mkImplies(
                                    ctx.mkEq(cs.subgroup(prev), ctx.mkAdd(ctx.mkInt(1), cs.subgroup(cell.id))),
                                    ctx.mkOr(*subGroups)
                                )
                            )
                        ))

                        rs.add(ctx.mkImplies(
                                ctx.mkAnd(
                                    matchRule(ctx, prev, prod),
                                    ctx.mkNot(ctx.mkEq(cs.group(prev), cs.group(cell.id)))
                            ),
                            ctx.mkEq(cs.subgroup(prev), ctx.mkInt(g.id(last))))
                        )
                    }
                    fun lastCell() {
                        rs.add(
                            ctx.mkImplies(
                                matchRule(ctx, cell.id, prod),
                                ctx.mkEq(cs.subgroup(cell.id), ctx.mkInt(g.id(last)))
                            )
                        )
                    }
                    firstCell()
                    if (!cell.firstColumn) {
                        adjCells()
                    }
                    if (cell.lastColumn) {
                        lastCell()
                    }
                }
                return rs
            }

            if (cell.firstRow) {
                rs.addAll(firstRow())
                rs.addAll(termConstraints())
            } else {
                rs.addAll(productConstraints())
                rs.addAll(sumConstraints())
            }
            rs.add(ruleConstraint())

        }
        return rs
    }
}