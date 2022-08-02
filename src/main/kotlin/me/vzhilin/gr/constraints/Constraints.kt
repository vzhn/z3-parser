package me.vzhilin.gr.constraints

import me.vzhilin.gr.constraints.exp.And
import me.vzhilin.gr.constraints.exp.ColumnId
import me.vzhilin.gr.constraints.exp.Const
import me.vzhilin.gr.constraints.exp.Eq
import me.vzhilin.gr.constraints.exp.Or
import me.vzhilin.gr.constraints.exp.Exp
import me.vzhilin.gr.constraints.exp.GroupId
import me.vzhilin.gr.constraints.exp.Iff
import me.vzhilin.gr.constraints.exp.Impl
import me.vzhilin.gr.constraints.exp.Inc
import me.vzhilin.gr.constraints.exp.Index
import me.vzhilin.gr.constraints.exp.ProductionTypeId
import me.vzhilin.gr.constraints.exp.ProductionTypeId.Companion.PROD
import me.vzhilin.gr.constraints.exp.RowId
import me.vzhilin.gr.constraints.exp.RuleId
import me.vzhilin.gr.constraints.exp.SubGroupId
import me.vzhilin.gr.constraints.exp.Zero
import me.vzhilin.gr.constraints.exp.eq
import me.vzhilin.gr.constraints.exp.ge
import me.vzhilin.gr.constraints.exp.le
import me.vzhilin.gr.constraints.exp.neq
import me.vzhilin.gr.model.CellPosition
import me.vzhilin.gr.rules.Prod
import me.vzhilin.gr.rules.Rule

typealias HorizontalHandler = Config.(left: CellPosition, right: CellPosition) -> Exp
typealias VerticalHandler = Config.(upper: CellPosition, bottom: CellPosition) -> Exp
typealias CellHandler = Config.(cell: CellPosition) -> Exp
typealias ColumnHandler = Config.(column: Int, cells: List<CellPosition>) -> Exp
typealias FirstColumnHandler = Config.(cell: CellPosition) -> Exp
typealias RowHandler = Config.(row: Int, cells: List<CellPosition>) -> Exp
typealias QuadHandler = Config.(left: CellPosition, right: CellPosition, bottomLeft: CellPosition, bottomRight: CellPosition) -> Exp

sealed class Constraints {
    data class FirstColumn(val handler: FirstColumnHandler): Constraints()
    data class HorizontalPair(val handler: HorizontalHandler): Constraints()
    data class VerticalPair(val handler: VerticalHandler): Constraints()
    data class Quad(val handler: QuadHandler): Constraints()
    data class Column(val handler: ColumnHandler): Constraints()
    data class Row(val handler: RowHandler): Constraints()
    data class Single(val handler: CellHandler): Constraints()
}

val BasicRanges = Constraints.Single { cell ->
    And(
        And(
            RowId(cell) eq Const(cell.row),
            ColumnId(cell) eq Const(cell.col),
        ),
        And(
            RuleId(cell) ge Zero, RuleId(cell) le Const(grammar.size - 1),
            ProductionTypeId(cell) ge Zero, ProductionTypeId(cell) le Const(2),
            GroupId(cell) ge Zero, GroupId(cell) le Const(columns - 1),
            SubGroupId(cell) ge Zero, SubGroupId(cell) le Const(columns - 1),
            Index(cell) ge Zero, Index(cell) le Const(columns - 1),
        )
    )
}

val StartFields = Constraints.FirstColumn { cell ->
    And(Eq(GroupId(cell), Zero),
        Eq(SubGroupId(cell), Zero),
        Eq(Index(cell), Zero))
}

val AdjGroupId = Constraints.HorizontalPair { left: CellPosition, right: CellPosition ->
    val leftGroupId = GroupId(left)
    val rightGroupId = GroupId(right)
    Or(leftGroupId eq rightGroupId, Inc(leftGroupId) eq rightGroupId)
}

// left.groupId = right.groupId => right.subGroupId = left.subGroupId + 1 || right.subGroupId = 0
val AdjSubGroupId = Constraints.HorizontalPair { left: CellPosition, right: CellPosition ->
    val leftGroupId = GroupId(left)
    val rightGroupId = GroupId(right)
    val leftSubGroupId = SubGroupId(left)
    val rightSubGroupId = SubGroupId(right)
    And(
        Impl(leftGroupId eq rightGroupId,
            Or(Inc(leftSubGroupId) eq rightSubGroupId, leftSubGroupId eq rightSubGroupId)),
        Impl(leftGroupId neq rightGroupId, Zero eq rightSubGroupId)
    )
}

// left.groupId = right.groupId => rightIndex = leftIndex + 1
val AdjCellIndex = Constraints.HorizontalPair { left: CellPosition, right: CellPosition ->
    val leftGroupId = GroupId(left)
    val rightGroupId = GroupId(right)
    val leftIndex = Index(left)
    val rightIndex = Index(right)
    And(
        Impl(leftGroupId eq rightGroupId, Inc(leftIndex) eq rightIndex),
        Impl(leftGroupId neq rightGroupId, rightIndex eq Zero)
    )
}

val DontDivideGroup = Constraints.VerticalPair { upper, bottom ->
    Impl(Index(upper) neq Zero, Index(bottom) neq Zero)
}

val SameGroupIdImplSameRuleId = Constraints.HorizontalPair { left, right ->
    Impl(
        GroupId(left) eq GroupId(right),
        RuleId(left) eq RuleId(right)
    )
}

val SameRuleIdImplSameRuleType = Constraints.HorizontalPair { left, right ->
    Impl(
        RuleId(left) eq RuleId(right),
        ProductionTypeId(left) eq ProductionTypeId(right)
    )
}

val SubGroupIdAlwaysZeroForNonProductionRules = Constraints.Single { cell ->
    Impl(ProductionTypeId(cell) neq PROD, SubGroupId(cell) eq Zero)
}

val DiffSubGroupIdIffDiffGroupId = Constraints.Quad { left, right, leftBottom, rightBottom ->
    Impl(
        And(ProductionTypeId(left) eq PROD,
            GroupId(left) eq GroupId(right)),
        Iff(
            SubGroupId(left) eq SubGroupId(right),
            GroupId(leftBottom) eq GroupId(rightBottom)
        )
    )
}

fun prodRuleConstraints(r: Prod): List<Constraints> {
    val args = r.components.map(Rule::id).map(::Const)
    fun isProd(cell: CellPosition) = And(
        RuleId(cell) eq Const(r.id),
        ProductionTypeId(cell) eq PROD
    )

    val rs = mutableListOf<Constraints>()
    rs.add(Constraints.Quad { left, right, bottomLeft, bottomRight ->
        val orExp = Or(args.zipWithNext().mapIndexed {
                index, (lhs, rhs) -> And(
            RuleId(bottomLeft) eq lhs,
            RuleId(bottomRight) eq rhs,
            SubGroupId(left) eq Const(index),
            SubGroupId(right) eq Const(index + 1)
        )
        })
        val cases = mutableListOf<Exp>()

        // start
        cases.add(Impl(
            And(isProd(right), GroupId(left) neq GroupId(right)),
            And(
                SubGroupId(right) eq Zero,
                RuleId(bottomRight) eq args.first()
            )))

        if (left.col == 0) {
            cases.add(Impl(isProd(left), And(SubGroupId(left) eq Zero, RuleId(bottomLeft) eq args.first())))
        }

        // middle
        cases.add(
            Impl(
                And(isProd(left), isProd(right), GroupId(left) eq GroupId(right)),
                Or(
                    And(
                        SubGroupId(left) eq SubGroupId(right),
                        GroupId(bottomLeft) eq GroupId(bottomRight)
                    ),
                    And(
                        Inc(SubGroupId(left)) eq SubGroupId(right),
                        Inc(GroupId(bottomLeft)) eq GroupId(bottomRight),
                        orExp
                    )
                )
            )
        )

        // finish
        cases.add(Impl(
            And(isProd(left), GroupId(left) neq GroupId(right)),
            And(
                SubGroupId(left) eq Const(args.lastIndex),
                RuleId(bottomRight) eq args.last()
            )
        ))

        if (right.col == columns - 1) {
            cases.add(Impl(
                isProd(right),
                And(
                    SubGroupId(right) eq Const(args.lastIndex),
                    RuleId(bottomRight) eq args.last()
                )
            ))
        }

        And(cases)
    })
    return rs
}
fun Config.allConstraints(): List<Constraints> {
    return listOf(
        BasicRanges,
        StartFields,
        AdjGroupId,
        AdjSubGroupId,
        AdjCellIndex,
        DontDivideGroup,
        SameGroupIdImplSameRuleId,
        SameRuleIdImplSameRuleType,
        SubGroupIdAlwaysZeroForNonProductionRules,
        DiffSubGroupIdIffDiffGroupId
    ) + grammar.prods.flatMap(::prodRuleConstraints)
}