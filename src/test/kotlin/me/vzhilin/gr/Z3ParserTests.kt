package me.vzhilin.gr

import com.microsoft.z3.Context
import com.microsoft.z3.IntNum
import com.microsoft.z3.Solver
import me.vzhilin.gr.report.writeSvg
import java.io.File
import kotlin.test.Test

class Z3Tests {
    enum class Fields { GROUP, SUBGROUP, RULE, CELL }

    @Test
    fun test() {
        solve("λx.y", 4)
    }

    fun lambdaCalculus(): Grammar {
        val X = Term("X", 'x')
        val Y = Term("Y", 'y')
        val DOT = Term("DOT", '.')
        val LAMBDA = Term("LAMBDA", 'λ')

        /* T = V | APP | ABST */
        val T = Sum("T", "V", "APP", "ABST")

        /* V = A | B | C | D */
        val V = Sum("V", "X", "Y")

        /* APP = T T */
        val APP = Prod("APP", "T", "T")

        /* ABST = λV.T */
        val ABST = Prod("ABST", "LAMBDA", "V", "DOT", "T")

        return Grammar(X, Y, DOT, LAMBDA, T, V, APP, ABST)
    }

    fun solve(input: String, rows: Int) {
        val grammar = lambdaCalculus()
        val ctx = Context()
        val solver = ctx.mkSolver()

        val cells = CellsContainer(rows, input.length, ctx)
        val cellAssertions = CellAssertions(cells)
        solver.add(*cellAssertions.make(ctx).toTypedArray())

        val grammarAssertions = GrammarAssertions(grammar, cells)
        solver.add(*grammarAssertions.make(ctx).toTypedArray())

        val inputAssertions = InputAssertions(input, grammar, cells)
        solver.add(*inputAssertions.make(ctx).toTypedArray())
        solver.check()
        println(solver.model)

        val model = getModel(solver, cells)
        val map = model.keys.groupBy { cell -> cell.row }.mapValues {
            (_, cells) -> cells.groupBy {
                val map = model[it]!!
                val groupId = map[Fields.GROUP]!!
                val ruleId = map[Fields.RULE]!!
                groupId to ruleId
            }.mapValues {
            (_, cells) -> cells.groupBy { model[it]!![Fields.SUBGROUP]!! }
            }
        }

        writeSvg(File("out1.svg"), input, grammar, map)
    }

    private fun getModel(solver: Solver, cells: CellsContainer): Map<Cell, Map<Fields, Int>> {
        val rs = mutableMapOf<Cell, Map<Fields, Int>>()
        cells.forEach { cell ->
            rs[cell] = Fields.values().associate { field ->
                val const = cells.const(cell.id, field)
                val fieldValue = (solver.model.getConstInterp(const) as IntNum).int
                field to fieldValue
            }
        }
        return rs
    }
}
