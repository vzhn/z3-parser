package me.vzhilin.gr.next

import me.vzhilin.gr.Grammar
import me.vzhilin.gr.next.constraints.StartFields
import me.vzhilin.gr.next.constraints.AdjGroupId

enum class RuleType { TERM, SUM, PROD }

sealed class Constraints {
    data class FirstColumn(val handler: FirstColumnHandler): Constraints()
    data class HorizontalPair(val handler: HorizontalHandler): Constraints()
    data class VerticalPair(val handler: VerticalHandler): Constraints()
    data class Column(val handler: ColumnHandler): Constraints()
    data class Row(val handler: RowHandler): Constraints()
    data class Cell(val handler: CellHandler): Constraints()
}

interface Environment {
    val grammar: Grammar
    val rows: Int
    val columns: Int
}

data class Cell(val row: Int, val col: Int) {
    override fun toString() = "Cell($row, $col)"
}

sealed class Exp
sealed class NatExp

object Zero: NatExp() {
    override fun toString(): String {
        return "0"
    }
}
object One: NatExp() {
    override fun toString(): String {
        return "1"
    }
}
data class Inc(val n: NatExp): NatExp() {
    override fun toString(): String {
        return "$n + 1"
    }
}
data class Const(val n: Int): NatExp() {
    override fun toString(): String {
        return "$n"
    }
}
sealed class CellField: NatExp() {
    abstract val cell: Cell
}
data class RowId(override val cell: Cell): CellField() {
    override fun toString() = "$cell.rowId"
}
data class ColumnId(override val cell: Cell): CellField() {
    override fun toString() = "$cell.columnId"
}
data class RuleTypeId(override val cell: Cell): CellField() {
    override fun toString() = "$cell.ruleTypeId"
}
data class RuleId(override val cell: Cell): CellField() {
    override fun toString() = "$cell.rowId"
}
data class GroupId(override val cell: Cell): CellField() {
    override fun toString() = "$cell.groupId"
}
data class SubGroupId(override val cell: Cell): CellField() {
    override fun toString() = "$cell.subGroupId"
}
data class Index(override val cell: Cell): CellField() {
    override fun toString() = "$cell.index"
}

data class Ge(val lhs: NatExp, val rhs: NatExp): Exp() {
    override fun toString() = "$lhs >= $rhs"
}
data class Le(val lhs: NatExp, val rhs: NatExp): Exp() {
    override fun toString() = "$lhs <= $rhs"
}
data class Eq(val lhs: NatExp, val rhs: NatExp): Exp() {
    override fun toString() = "$lhs = $rhs"
}
data class Neq(val lhs: NatExp, val rhs: NatExp): Exp() {
    override fun toString(): String {
        return "$lhs != $rhs"
    }
}
data class Impl(val lhs: Exp, val rhs: Exp): Exp() {
    override fun toString(): String {
        return "$lhs => $rhs"
    }
}
data class Or(val exps: List<Exp>): Exp() {
    constructor(vararg exps: Exp): this(exps.toList())

    override fun toString(): String {
       return exps.joinToString(" || ")
    }
}
data class And(val exps: List<Exp>): Exp() {
    constructor(vararg exps: Exp): this(exps.toList())
    override fun toString(): String {
        return exps.joinToString(" && ")
    }
}

typealias HorizontalHandler = Environment.(left: Cell, right: Cell) -> Exp
typealias VerticalHandler = Environment.(upper: Cell, bottom: Cell) -> Exp
typealias CellHandler = Environment.(cell: Cell) -> Exp
typealias ColumnHandler = Environment.(column: Int, cells: List<Cell>) -> Exp
typealias FirstColumnHandler = Environment.(cell: Cell) -> Exp
typealias RowHandler = Environment.(row: Int, cells: List<Cell>) -> Exp

class CellMatrix {
    private val constraints = mutableListOf<Constraints>()

    init {
        constraints.add(StartFields)
        constraints.add(AdjGroupId)
    }
}