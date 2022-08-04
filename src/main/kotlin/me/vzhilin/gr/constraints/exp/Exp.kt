package me.vzhilin.gr.constraints.exp

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
    abstract val rowId: Int
    abstract val colId: Int
}

data class ProductionTypeId(
    override val rowId: Int,
    override val colId: Int
): CellField() {
    companion object {
        val BYPASS = Const(0)
        val SUM = Const(1)
        val PROD = Const(2)
    }
}
data class RuleId(override val rowId: Int,
                  override val colId: Int): CellField()

data class GroupId(    override val rowId: Int,
                       override val colId: Int): CellField()


data class SubGroupId(    override val rowId: Int,
                          override val colId: Int): CellField()


data class Index(    override val rowId: Int,
                     override val colId: Int): CellField()
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
data class Impl(val label: String = "", val lhs: Exp, val rhs: Exp): Exp() {
    constructor(lhs: Exp, rhs: Exp): this("", lhs, rhs)
    override fun toString(): String {
        return "$lhs => $rhs"
    }

    fun label(label: String): Impl {
        return Impl(label, lhs, rhs)
    }
}
data class Iff(val lhs: Exp, val rhs: Exp): Exp() {
    override fun toString(): String {
        return "$lhs <=> $rhs"
    }
}
data class Or(val label: String = "", val exps: List<Exp>): Exp() {
    constructor(vararg exps: Exp): this("", exps.toList())
    constructor(exps: List<Exp>): this("", exps)

    override fun toString(): String {
       return exps.joinToString(" || ")
    }

    fun label(label: String): Or {
        return Or(label, exps)
    }
}
data class And(val label: String = "", val exps: List<Exp>): Exp() {
    constructor(vararg exps: Exp): this("", exps.toList())
    constructor(exps: List<Exp>): this("", exps)

    override fun toString(): String {
        return exps.joinToString(" && ")
    }

    fun label(label: String): And {
        return And(label, exps)
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
