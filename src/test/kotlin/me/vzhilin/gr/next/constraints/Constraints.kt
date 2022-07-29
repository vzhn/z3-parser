package me.vzhilin.gr.next.constraints

import me.vzhilin.gr.next.*

val BasicRanges = Constraints.Cell { cell ->
    And(
        And(
            RowId(cell) eq Const(cell.row),
            ColumnId(cell) eq Const(cell.col),
        ),
        And(
            RuleId(cell) ge Zero, RuleId(cell) le Const(grammar.size - 1),
            RuleTypeId(cell) ge Zero, RuleTypeId(cell) le Const(2),
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

val AdjGroupId = Constraints.HorizontalPair { left: Cell, right: Cell ->
    val leftGroupId = GroupId(left)
    val rightGroupId = GroupId(right)
    Or(leftGroupId eq rightGroupId, Inc(leftGroupId) eq rightGroupId)
}

// left.groupId = right.groupId => right.subGroupId = left.subGroupId + 1 || right.subGroupId = 0
val AdjSubGroupId = Constraints.HorizontalPair { left: Cell, right: Cell ->
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
val AdjCellIndex = Constraints.HorizontalPair { left: Cell, right: Cell ->
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

infix fun NatExp.eq(rhs: NatExp): Exp {
    return Eq(this, rhs)
}

infix fun NatExp.neq(rhs: NatExp): Exp {
    return Neq(this, rhs)
}

infix fun NatExp.ge(rhs: NatExp): Exp {
    return Ge(this, rhs)
}

infix fun NatExp.le(rhs: NatExp): Exp {
    return Le(this, rhs)
}
