package me.vzhilin.gr.smt

import com.microsoft.z3.*
import me.vzhilin.gr.constraints.exp.*

class SMTRoutine(
    private val cols: Int,
    private val rows: Int,
    private val exps: List<Exp>
) {
    private val context = Context()

    private fun createSolver() {
        val solver = context.mkSolver("LIA")
        solver.add(*exps.map { convBool(it) }.toTypedArray())
    }

    private fun mkConst(row: Int, col: Int, field: String): ArithExpr<IntSort> {
        return context.mkIntConst("${field}(${row},${col})")
    }

    private fun convNat(e: NatExp): ArithExpr<IntSort> {
        return when (e) {
            is ColumnId -> mkConst(e.cell.row, e.cell.col, "columnId")
            is GroupId -> mkConst(e.cell.row, e.cell.col, "groupId")
            is Index -> mkConst(e.cell.row, e.cell.col, "index")
            is ProductionTypeId -> mkConst(e.cell.row, e.cell.col, "productionTypeId")
            is RowId -> mkConst(e.cell.row, e.cell.col, "rowId")
            is RuleId -> mkConst(e.cell.row, e.cell.col, "ruleId")
            is SubGroupId -> mkConst(e.cell.row, e.cell.col, "subGroupId")
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