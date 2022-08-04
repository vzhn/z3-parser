package me.vzhilin.gr.constraints

import me.vzhilin.gr.constraints.exp.And
import me.vzhilin.gr.constraints.exp.CellField
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
import me.vzhilin.gr.constraints.exp.ProductionTypeId.Companion.PROD
import me.vzhilin.gr.constraints.exp.RuleId
import me.vzhilin.gr.constraints.exp.SubGroupId
import me.vzhilin.gr.constraints.exp.Zero
import me.vzhilin.gr.constraints.exp.eq
import me.vzhilin.gr.constraints.exp.ge
import me.vzhilin.gr.constraints.exp.le
import me.vzhilin.gr.constraints.exp.neq
import me.vzhilin.gr.rules.*
import me.vzhilin.gr.smt.Cells

typealias HorizontalHandler = (rowId: Int, leftColId: Int, rightColId: Int) -> Exp
typealias VerticalHandler = (colId: Int, rowIdUpper: Int, rowIdBottom: Int) -> Exp
typealias CellHandler = (rowId: Int, colId: Int) -> Exp
typealias ColumnHandler = (columnId: Int, rowIds: List<Int>) -> Exp
typealias FirstColumnHandler = (rowId: Int, colId: Int) -> Exp
typealias FirstRowHandler = (colId: Int) -> Exp
typealias RowHandler = (rowId: Int, colIds: List<Int>) -> Exp
typealias QuadHandler = (
    leftColId: Int, leftRowId: Int,
    rightColId: Int, rightRowId: Int,
    bottomLeftColId: Int, bottomLeftRowId: Int,
    bottomRightColId: Int, bottomRightRowId: Int,
) -> Exp

sealed class Constraints {
    data class FirstColumn(val handler: FirstColumnHandler): Constraints()
    data class FirstRow(val handler: FirstRowHandler): Constraints()
    data class HorizontalPair(val handler: HorizontalHandler): Constraints()
    data class VerticalPair(val handler: VerticalHandler): Constraints()
    data class Quad(val handler: QuadHandler): Constraints()
    data class Column(val handler: ColumnHandler): Constraints()
    data class Row(val handler: RowHandler): Constraints()
    data class Single(val handler: CellHandler): Constraints()
}

fun BasicRanges(g: Grammar, rows: Int, cols: Int) = Constraints.Single { rowId, colId ->
    val ruleId = RuleId(rowId, colId)
    val productionTypeId = ProductionTypeId(rowId, colId)
    val groupId = GroupId(rowId, colId)
    val subGroupId = SubGroupId(rowId, colId)
    val index = Index(rowId, colId)
    And(
        ruleId ge Zero, ruleId le Const(g.size - 1),
        productionTypeId ge Zero, productionTypeId le Const(2),
        groupId ge Zero, groupId le Const(cols - 1),
        subGroupId ge Zero, subGroupId le Const(cols - 1),
        index ge Zero, index le Const(cols - 1),
    ).label("BasicRanges")
}

val StartFields = Constraints.FirstColumn { rowId, colId ->
    And(Eq(GroupId(rowId, colId), Zero),
        Eq(SubGroupId(rowId, colId), Zero),
        Eq(Index(rowId, colId), Zero)).label("StartFields")
}

fun FirstRow(g: Grammar, input: String) = Constraints.FirstRow { colId ->
    And(
        RuleId(0, colId) eq Const(g[input[colId]].id),
        GroupId(0, colId) eq Const(colId)
    ).label("FirstRow")
}

val AdjGroupId = Constraints.HorizontalPair { rowId: Int, leftColId: Int,  rightColId: Int ->
    val leftGroupId = GroupId(rowId, leftColId)
    val rightGroupId = GroupId(rowId, rightColId)
    Or(leftGroupId eq rightGroupId, Inc(leftGroupId) eq rightGroupId).label("AdjGroupId")
}

// left.groupId = right.groupId => right.subGroupId = left.subGroupId + 1 || right.subGroupId = 0
val AdjSubGroupId = Constraints.HorizontalPair { rowId: Int, leftColId: Int,  rightColId: Int ->
    val leftGroupId = GroupId(rowId, leftColId)
    val rightGroupId = GroupId(rowId, rightColId)
    val leftSubGroupId = SubGroupId(rowId, leftColId)
    val rightSubGroupId = SubGroupId(rowId, rightColId)
    And(
        Impl(leftGroupId eq rightGroupId,
            Or(Inc(leftSubGroupId) eq rightSubGroupId, leftSubGroupId eq rightSubGroupId)),
        Impl(leftGroupId neq rightGroupId, Zero eq rightSubGroupId)
    ).label("AdjSubGroupId")
}

// left.groupId = right.groupId => rightIndex = leftIndex + 1
val AdjCellIndex = Constraints.HorizontalPair { rowId: Int, leftColId: Int,  rightColId: Int  ->
    val leftGroupId = GroupId(rowId, leftColId)
    val rightGroupId = GroupId(rowId, rightColId)
    val leftIndex = Index(rowId, leftColId)
    val rightIndex = Index(rowId, rightColId)
    And(
        Impl(leftGroupId eq rightGroupId, Inc(leftIndex) eq rightIndex),
        Impl(leftGroupId neq rightGroupId, rightIndex eq Zero)
    ).label("AdjCellIndex")
}

val DontDivideGroup = Constraints.VerticalPair { colId: Int, rowIdUpper: Int, rowIdBottom: Int ->
    Impl(Index(rowIdBottom, colId) neq Zero, Index(rowIdUpper, colId) neq Zero).label("DontDivideGroup")
}

val SameGroupIdImplSameRuleId = Constraints.HorizontalPair { rowId, leftColId, rightColId ->
    Impl(
        GroupId(rowId, leftColId) eq GroupId(rowId, rightColId),
        RuleId(rowId, leftColId) eq RuleId(rowId, rightColId)
    ).label("SameGroupIdImplSameRuleId")
}

val SameRuleIdImplSameRuleType = Constraints.HorizontalPair { rowId, leftColId, rightColId ->
    Impl(
        RuleId(rowId, leftColId) eq RuleId(rowId, rightColId),
        ProductionTypeId(rowId, leftColId) eq ProductionTypeId(rowId, rightColId)
    ).label("SameRuleIdImplSameRuleType")
}

val SubGroupIdAlwaysZeroForNonProductionRules = Constraints.Single { rowId, colId ->
    Impl(ProductionTypeId(rowId, colId) neq PROD, SubGroupId(rowId, colId) eq Zero).label("SubGroupIdAlwaysZeroForNonProductionRules")
}

val DiffSubGroupIdIffDiffGroupId = Constraints.Quad {
        leftRowId, leftColId, rightRowId, rightColId,
        bottomLeftRowId, bottomLeftColId, bottomRightRowId, bottomRightColId ->
    Impl(
        And(ProductionTypeId(leftRowId, leftColId) eq PROD,
            GroupId(leftRowId, leftColId) eq GroupId(rightRowId, rightColId)),
        Iff(
            SubGroupId(leftRowId, leftColId) eq SubGroupId(rightRowId, rightColId),
            GroupId(bottomLeftRowId, bottomLeftColId) eq GroupId(bottomRightRowId, bottomRightColId)
        )
    ).label("DiffSubGroupIdIffDiffGroupId")
}

fun prodRuleConstraints(r: Prod, rows: Int, cols: Int): List<Constraints> {
    val args = r.components.map(Rule::id).map(::Const)
    fun isProd(rowId: Int, colId: Int) = And(
        RuleId(rowId, colId) eq Const(r.id),
        ProductionTypeId(rowId, colId) eq PROD
    )

    val rs = mutableListOf<Constraints>()
    rs.add(Constraints.Quad { leftRowId, leftColId, rightRowId, rightColId,
                              bottomLeftRowId, bottomLeftColId, bottomRightRowId, bottomRightColId ->

        val orExp = Or(args.zipWithNext().mapIndexed { index, (lhs, rhs) ->
            And(
                RuleId(bottomLeftRowId, bottomLeftColId) eq lhs,
                RuleId(bottomRightRowId, bottomRightColId) eq rhs,
                SubGroupId(leftRowId, leftColId) eq Const(index),
                SubGroupId(rightRowId, rightColId) eq Const(index + 1)
            ).label("ProdMiddleCase")
        })
        val cases = mutableListOf<Exp>()

        // start
        cases.add(
            Impl(
                And(isProd(rightRowId, rightColId), GroupId(leftRowId, leftColId) neq GroupId(rightRowId, rightColId)),
                And(
                    SubGroupId(rightRowId, rightColId) eq Zero,
                    RuleId(bottomRightRowId, bottomRightColId) eq args.first()
                )
            ).label("ProdStart")
        )

        if (leftColId == 0) {
            cases.add(
                Impl(
                    isProd(leftRowId, leftColId),
                    And(
                        SubGroupId(leftRowId, leftColId) eq Zero,
                        RuleId(bottomLeftRowId, bottomLeftColId) eq args.first()
                    )
                ).label("ProdLeftCorner")
            )
        }

        // middle
        cases.add(
            Impl(
                And(
                    isProd(leftRowId, leftColId),
                    isProd(rightRowId, rightColId),
                    GroupId(leftRowId, leftColId) eq GroupId(rightRowId, rightColId)
                ),
                Or(
                    And(
                        SubGroupId(leftRowId, leftColId) eq SubGroupId(rightRowId, rightColId),
                        GroupId(bottomLeftRowId, bottomLeftColId) eq GroupId(bottomRightRowId, bottomRightColId)
                    ),
                    And(
                        Inc(SubGroupId(leftRowId, leftColId)) eq SubGroupId(rightRowId, rightColId),
                        Inc(GroupId(bottomLeftRowId, bottomLeftColId)) eq GroupId(bottomRightRowId, bottomRightColId),
                        orExp
                    )
                )
            ).label("ProdMiddle")
        )

        // finish
        cases.add(
            Impl(
                And(isProd(leftRowId, leftColId), GroupId(leftRowId, leftColId) neq GroupId(rightRowId, rightColId)),
                And(
                    SubGroupId(leftRowId, leftColId) eq Const(args.lastIndex),
                    RuleId(bottomLeftRowId, bottomLeftColId) eq args.last()
                )
            ).label("ProdFinish")
        )

        if (rightColId == cols - 1) {
            cases.add(
                Impl(
                    isProd(rightRowId, rightColId),
                    And(
                        SubGroupId(rightRowId, rightColId) eq Const(args.lastIndex),
                        RuleId(bottomRightRowId, bottomRightColId) eq args.last()
                    )
                ).label("ProdRightCorner")
            )
        }

        And(cases).label("Prod")
    })
    return rs
}

fun sumRuleConstraints(s: Sum, rows: Int, cols: Int): List<Constraints> {
    return emptyList()
}

fun allConstraints(grammar: Grammar, rows: Int, input: String): List<Constraints> {
    val cols = input.length
    return listOf(
        BasicRanges(grammar, rows, cols),
        StartFields,
        FirstRow(grammar, input),
        AdjGroupId,
        AdjSubGroupId,
        AdjCellIndex,
        DontDivideGroup,
        SameGroupIdImplSameRuleId,
        SameRuleIdImplSameRuleType,
        SubGroupIdAlwaysZeroForNonProductionRules,
        DiffSubGroupIdIffDiffGroupId,
    ) + grammar.prods.flatMap { prodRuleConstraints(it, rows, cols) } +
        grammar.sums.flatMap { sumRuleConstraints(it, rows, cols) }
}

fun List<Constraints>.toExpressions(rows: Int, cols: Int): List<Exp> {
    val expressions = mutableListOf<Exp>()
    forEach { c ->
        when (c) {
            is Constraints.Single -> {
                cartesianProduct(rows, cols).map { (rowId, colId) ->
                    c.handler(rowId, colId)
                }.toCollection(expressions)
            }
            is Constraints.FirstColumn -> {
                cartesianProduct(rows, 1).map { (rowId, colId) ->
                    c.handler(rowId, colId)
                }.toCollection(expressions)
            }
            is Constraints.VerticalPair -> {
                for (rowId in 1 until rows) {
                    for (colId in 0 until cols) {
                        expressions.add(c.handler(colId, rowId, rowId - 1))
                    }
                }
            }
            is Constraints.HorizontalPair -> {
                for (rowId in 0 until rows) {
                    for (colId in 1 until cols) {
                        expressions.add(c.handler(rowId, colId - 1, colId))
                    }
                }
            }
            is Constraints.Quad -> {
                for (rowId in 1 until rows) {
                    for (colId in 1 until cols) {
                        expressions.add(c.handler(
                            rowId,     colId - 1, rowId,     colId,
                            rowId - 1, colId - 1, rowId - 1, colId
                        ))
                    }
                }
            }
            is Constraints.Column -> {
                for (colId in 0 until cols) {
                    expressions.add(c.handler(colId, (0 until rows).toList()))
                }
            }
            is Constraints.Row -> {
                for (rowId in 0 until rows) {
                    expressions.add(c.handler(rowId, (0 until cols).toList()))
                }
            }
            is Constraints.FirstRow -> {
                for (colId in 0 until cols) {
                    expressions.add(c.handler(colId))
                }
            }
        }
    }
    return expressions
}

fun List<Constraints>.validate(cells: Cells, rows: Int, cols: Int): Boolean {
    val expressions = toExpressions(rows, cols)
    val failedExp = expressions.firstOrNull { !cells.ev(it) }
    return failedExp == null
}


private fun Cells.ev(exp: NatExp): Int = when (exp) {
    is CellField -> {
        when (exp) {
            is GroupId -> getGroupId(exp.rowId, exp.colId)
            is Index -> getIndex(exp.rowId, exp.colId)
            is RuleId -> getRuleId(exp.rowId, exp.colId)
            is ProductionTypeId -> getProductionTypeId(exp.rowId, exp.colId)
            is SubGroupId -> getSubGroupId(exp.rowId, exp.colId)
        }
    }

    is Const -> exp.n
    is Inc -> ev(exp.n) + 1
    One -> 1
    Zero -> 0
}

private fun Cells.ev(e: Exp): Boolean {
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

private fun cartesianProduct(rows: Int, columns: Int): List<Pair<Int, Int>> {
    val pairs = mutableListOf<Pair<Int, Int>>()
    for (rowId in 0 until rows) {
        for (colId in 0 until columns) {
            pairs.add(rowId to colId)
        }
    }
    return pairs
}

fun Cells.validate(vararg cons: Constraints): Boolean {
    val expressions = cons.toList().toExpressions(rows, cols)
    val failedExp = expressions.firstOrNull { !ev(it) }
    return failedExp == null
}