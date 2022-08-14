package me.vzhilin.gr.parser.smt

import com.microsoft.z3.*
import me.vzhilin.gr.parser.And
import me.vzhilin.gr.parser.Const
import me.vzhilin.gr.parser.Eq
import me.vzhilin.gr.parser.Exp
import me.vzhilin.gr.parser.Ge
import me.vzhilin.gr.parser.Grammar
import me.vzhilin.gr.parser.GroupId
import me.vzhilin.gr.parser.Gt
import me.vzhilin.gr.parser.Iff
import me.vzhilin.gr.parser.Impl
import me.vzhilin.gr.parser.Inc
import me.vzhilin.gr.parser.Index
import me.vzhilin.gr.parser.Le
import me.vzhilin.gr.parser.Lt
import me.vzhilin.gr.parser.NatExp
import me.vzhilin.gr.parser.Neq
import me.vzhilin.gr.parser.Not
import me.vzhilin.gr.parser.One
import me.vzhilin.gr.parser.Or
import me.vzhilin.gr.parser.ProductionTypeId
import me.vzhilin.gr.parser.Rule
import me.vzhilin.gr.parser.RuleId
import me.vzhilin.gr.parser.SubGroupId
import me.vzhilin.gr.parser.Zero

enum class Fields {
    GroupId,
    SubGroupId,
    ProductionTypeId,
    RuleId,
    Index,
}

data class Cells(
    val rows: Int,
    val cols: Int,
) {
    val rs = 0 until rows
    val cs = 0 until cols
    private val data = mutableMapOf<Fields, MutableMap<Pair<Int, Int>, Int>>()
    init {
        for (field in Fields.values()) {
            val map = mutableMapOf<Pair<Int, Int>, Int>()
            data[field] = map
            for (rowId in 0 until rows) {
                for (colId in 0 until cols) {
                    map[rowId to colId] = 0
                }
            }
        }
    }

    fun setRuleId(rowId: Int, g: Grammar, vararg rules: String): Unit = setRuleId(rowId, rules.map { g[it].id }.toTypedArray())
    fun getRule(g: Grammar, rowId: Int, colId: Int): Rule {
        return g[getRuleId(rowId, colId)]
    }
    fun setGroupId(rowId: Int, vararg ids: Int)    = setGroupId(rowId, ids.toTypedArray())
    fun setSubGroupId(rowId: Int, vararg ids: Int) = setSubGroupId(rowId, ids.toTypedArray())
    fun setIndex(rowId: Int, vararg ids: Int)      = setIndex(rowId, ids.toTypedArray())
    fun setProdTypeId(rowId: Int, vararg ids: Int)   = setProdTypeId(rowId, ids.toTypedArray())

    fun getGroupId(rowId: Int, colId: Int): Int    = data[Fields.GroupId]!![rowId to colId]!!
    fun getSubGroupId(rowId: Int, colId: Int): Int = data[Fields.SubGroupId]!![rowId to colId]!!
    fun setProdTypeId(rowId: Int, colId: Int): Int = data[Fields.ProductionTypeId]!![rowId to colId]!!
    fun getRuleId(rowId: Int, colId: Int): Int = data[Fields.RuleId]!![rowId to colId]!!
    fun getIndex(rowId: Int, colId: Int): Int = data[Fields.Index]!![rowId to colId]!!

    fun setGroupId(rowId: Int, indices: Array<Int>) {
        if (indices.size != cols) throw IllegalArgumentException("indices.size != cols")
        (0 until cols).forEach { colId -> setGroupId(rowId, colId, indices[colId]) }
    }
    fun setSubGroupId(rowId: Int, indices: Array<Int>) {
        if (indices.size != cols) throw IllegalArgumentException("indices.size != cols")
        (0 until cols).forEach { colId -> setSubGroupId(rowId, colId, indices[colId]) }
    }
    fun setProdTypeId(rowId: Int, indices: Array<Int>) {
        if (indices.size != cols) throw IllegalArgumentException("indices.size != cols")
        (0 until cols).forEach { colId -> setProductionTypeId(rowId, colId, indices[colId]) }
    }
    fun setRuleId(rowId: Int, indices: Array<Int>) {
        if (indices.size != cols) throw IllegalArgumentException("indices.size != cols")
        (0 until cols).forEach { colId -> setRuleId(rowId, colId, indices[colId]) }
    }
    fun setIndex(rowId: Int, indices: Array<Int>) {
        if (indices.size != cols) throw IllegalArgumentException("indices.size != cols")
        (0 until cols).forEach { colId -> setIndex(rowId, colId, indices[colId]) }
    }

    fun setGroupId(rowId: Int, colId: Int, v: Int) {
        data[Fields.GroupId]!![rowId to colId] = v
    }
    fun setSubGroupId(rowId: Int, colId: Int, v: Int) {
        data[Fields.SubGroupId]!![rowId to colId] = v
    }
    fun setProductionTypeId(rowId: Int, colId: Int, v: Int) {
        data[Fields.ProductionTypeId]!![rowId to colId] = v
    }
    fun setRuleId(rowId: Int, colId: Int, v: Int) {
        data[Fields.RuleId]!![rowId to colId] = v
    }
    fun setIndex(rowId: Int, colId: Int, v: Int) {
        data[Fields.Index]!![rowId to colId] = v
    }
}

sealed class SMTResult {
    object Unsat: SMTResult()
    object Unknown: SMTResult()
    data class Satisfiable(
        val cells: Cells
    ): SMTResult()
}

class SMTRoutine(
    private val rows: Int,
    private val cols: Int,
    private val exps: List<Exp>
) {
    private val context = Context()

    fun solve(): SMTResult {
        val solver = context.mkSolver("LIA")
        solver.add(*exps.map { convBool(it) }.toTypedArray())
        return when (solver.check()!!) {
            Status.UNSATISFIABLE -> SMTResult.Unsat
            Status.UNKNOWN -> SMTResult.Unknown
            Status.SATISFIABLE -> {
                return SMTResult.Satisfiable(extractModel(solver.model))
            }
        }
    }

    fun getField(n: String): Fields {
        return when (val field = n.substring(0 until n.indexOf('('))) {
            "index" -> Fields.Index
            "ruleId" -> Fields.RuleId
            "groupId" -> Fields.GroupId
            "subGroupId" -> Fields.SubGroupId
            "productionTypeId" -> Fields.ProductionTypeId
            else -> throw IllegalArgumentException("unknown field: '$field'")
        }
    }

    private fun getRow(n: String): Int {
        val start = n.indexOf('(') + 1
        val end = n.indexOf(',')
        return n.substring(start, end).trim().toInt()
    }

    private fun getCol(n: String): Int {
        val start = n.indexOf(',') + 1
        val end = n.indexOf(')')
        return n.substring(start, end).trim().toInt()
    }

    private fun extractModel(model: Model): Cells {
        val cells = Cells(rows, cols)

        model.constDecls.forEach { decl ->
            val name = (decl.name as StringSymbol).string
            val field = getField(name)
            val row = getRow(name)
            val col = getCol(name)
            val v = (model.getConstInterp(decl) as IntNum).int
            when (field) {
                Fields.GroupId -> cells.setGroupId(row, col, v)
                Fields.SubGroupId -> cells.setSubGroupId(row, col, v)
                Fields.ProductionTypeId -> cells.setProductionTypeId(row, col, v)
                Fields.RuleId -> cells.setRuleId(row, col, v)
                Fields.Index -> cells.setIndex(row, col, v)
            }
        }
        return cells
    }

    private fun mkConst(rowId: Int, colId: Int, field: String): ArithExpr<IntSort> {
        return context.mkIntConst("${field}(${rowId},${colId})")
    }

    private fun convNat(e: NatExp): ArithExpr<IntSort> {
        return when (e) {
            is GroupId -> mkConst(e.rowId, e.colId, "groupId")
            is Index -> mkConst(e.rowId, e.colId, "index")
            is ProductionTypeId -> mkConst(e.rowId, e.colId, "productionTypeId")
            is RuleId -> mkConst(e.rowId, e.colId, "ruleId")
            is SubGroupId -> mkConst(e.rowId, e.colId, "subGroupId")
            is Const -> context.mkInt(e.n)
            is Inc -> context.mkAdd(context.mkInt(1), convNat(e.n))
            One -> context.mkInt(1)
            Zero -> context.mkInt(0)
        }
    }

    private fun convBool(e: Exp): BoolExpr {
        return when (e) {
            is And -> context.mkAnd(*e.exps.map(::convBool).toTypedArray())
            is Or -> context.mkOr(*e.exps.map(::convBool).toTypedArray())
            is Iff -> context.mkIff(convBool(e.lhs), convBool(e.rhs))
            is Impl -> context.mkImplies(convBool(e.lhs), convBool(e.rhs))
            is Not -> context.mkNot(convBool(e.lhs))
            is Eq -> context.mkEq(convNat(e.lhs), convNat(e.rhs))
            is Neq -> context.mkNot(context.mkEq(convNat(e.lhs), convNat(e.rhs)))
            is Ge -> context.mkGe(convNat(e.lhs), convNat(e.rhs))
            is Gt -> context.mkGt(convNat(e.lhs), convNat(e.rhs))
            is Le -> context.mkLe(convNat(e.lhs), convNat(e.rhs))
            is Lt -> context.mkLt(convNat(e.lhs), convNat(e.rhs))
        }
    }
}

