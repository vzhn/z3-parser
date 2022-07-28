package me.vzhilin.gr.next

import me.vzhilin.gr.Grammar
import me.vzhilin.gr.next.constraints.StartFields
import me.vzhilin.gr.next.constraints.AdjGroupId

enum class RuleType { TERM, SUM, PROD }

sealed class Constraints {
    data class FirstColumn(val handler: FirstColumnHandler): Constraints()
    data class Horiz(val handler: HorizHandler): Constraints()
    data class Vert(val handler: VertHandler): Constraints()
    data class Column(val handler: ColumnHandler): Constraints()
    data class Row(val handler: RowHandler): Constraints()
    data class Cell(val handler: CellHandler): Constraints()
}

enum class Placement { FIRST, MIDDLE, LAST }

data class Placement2D(
    val horiz: Placement,
    val vert: Placement
) {
    val isFirstCell get() = horiz == Placement.FIRST && vert == Placement.FIRST
}

interface Environment {
    val grammar: Grammar
    val rows: Int
    val columns: Int
}

interface Cell {
    val row: Int
    val col: Int
}

sealed class Exp
sealed class NatExp

object Zero: NatExp()
object One: NatExp()
data class Inc(val n: NatExp): NatExp()

data class Const(val n: Int): NatExp()
data class RowId(val cell: Cell): NatExp()
data class ColumnId(val cell: Cell): NatExp()
data class RuleTypeId(val cell: Cell): NatExp()
data class RuleId(val cell: Cell): NatExp()
data class GroupId(val cell: Cell): NatExp()
data class SubGroupId(val cell: Cell): NatExp()
data class Index(val cell: Cell): NatExp()

data class Ge(val lhs: NatExp, val rhs: NatExp): Exp()
data class Le(val lhs: NatExp, val rhs: NatExp): Exp()
data class Eq(val lhs: NatExp, val rhs: NatExp): Exp()
data class Neq(val lhs: NatExp, val rhs: NatExp): Exp()
data class Impl(val lhs: Exp, val rhs: Exp): Exp()
data class Or(val exps: List<Exp>): Exp() {
    constructor(vararg exps: Exp): this(exps.toList())
}
data class And(val exps: List<Exp>): Exp() {
    constructor(vararg exps: Exp): this(exps.toList())
}

typealias HorizHandler = Environment.(left: Cell, right: Cell) -> Exp
typealias VertHandler = Environment.(upper: Cell, bottom: Cell) -> Exp
typealias CellHandler = Environment.(cell: Cell) -> Exp
typealias ColumnHandler = Environment.(column: Int, cells: List<Cell>) -> Exp
typealias FirstColumnHandler = Environment.(cell: Cell) -> Exp
typealias RowHandler = Environment.(row: Int, cells: List<Cell>) -> Exp


class Field {
    private val constraints = mutableListOf<Constraints>()

    init {
        constraints.add(StartFields)
        constraints.add(AdjGroupId)
    }
}