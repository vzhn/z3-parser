package me.vzhilin.gr.smt

import com.microsoft.z3.*
import me.vzhilin.gr.constraints.exp.*

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
    private val data: MutableMap<Fields, MutableMap<Pair<Int, Int>, Int>> = mutableMapOf()
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

    fun getGroupId(row: Int, col: Int): Int    = data[Fields.GroupId]!![row to col]!!
    fun getSubGroupId(row: Int, col: Int): Int = data[Fields.SubGroupId]!![row to col]!!
    fun getProductionTypeId(row: Int, col: Int): Int = data[Fields.ProductionTypeId]!![row to col]!!
    fun getRuleId(row: Int, col: Int): Int = data[Fields.RuleId]!![row to col]!!
    fun getIndex(row: Int, col: Int): Int = data[Fields.Index]!![row to col]!!

    fun setGroupId(rowId: Int, indices: Array<Int>) {
        if (indices.size != cols) throw IllegalArgumentException("indices.size != cols")
        (0 until cols).forEach { colId -> setGroupId(rowId, colId, indices[colId]) }
    }
    fun setSubGroupId(rowId: Int, indices: Array<Int>) {
        if (indices.size != cols) throw IllegalArgumentException("indices.size != cols")
        (0 until cols).forEach { colId -> setSubGroupId(rowId, colId, indices[colId]) }
    }
    fun setProductionTypeId(rowId: Int, indices: Array<Int>) {
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
    object UNSAT: SMTResult()
    object UNKNOWN: SMTResult()
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
        when (solver.check()!!) {
            Status.UNSATISFIABLE -> TODO()
            Status.UNKNOWN -> TODO()
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

    fun extractModel(model: Model): Cells {
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

    private fun mkConst(row: Int, col: Int, field: String): ArithExpr<IntSort> {
        return context.mkIntConst("${field}(${row},${col})")
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

