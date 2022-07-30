package me.vzhilin.gr.constraints.exp

import me.vzhilin.gr.model.Cell

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
data class ProductionTypeId(override val cell: Cell): CellField() {
    override fun toString() = "$cell.productionTypeId"
    companion object {
        val BYPASS = Const(0)
        val SUM = Const(1)
        val PROD = Const(2)
    }
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
data class Gt(val lhs: NatExp, val rhs: NatExp): Exp() {
    override fun toString() = "$lhs > $rhs"
}

data class Ge(val lhs: NatExp, val rhs: NatExp): Exp() {
    override fun toString() = "$lhs >= $rhs"
}

data class Lt(val lhs: NatExp, val rhs: NatExp): Exp() {
    override fun toString() = "$lhs < $rhs"
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
data class Iff(val lhs: Exp, val rhs: Exp): Exp() {
    override fun toString(): String {
        return "$lhs <=> $rhs"
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
data class Not(val lhs: Exp): Exp() {
    override fun toString(): String {
        return "!$lhs"
    }
}

infix fun NatExp.eq(rhs: NatExp) = Eq(this, rhs)
infix fun NatExp.neq(rhs: NatExp) = Neq(this, rhs)
infix fun NatExp.ge(rhs: NatExp) = Ge(this, rhs)
infix fun NatExp.le(rhs: NatExp) = Le(this, rhs)
infix fun NatExp.gt(rhs: NatExp) = Gt(this, rhs)
infix fun NatExp.lt(rhs: NatExp) = Lt(this, rhs)
