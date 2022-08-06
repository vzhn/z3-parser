package me.vzhilin.gr

import me.vzhilin.gr.constraints.allConstraints
import me.vzhilin.gr.constraints.validate
import me.vzhilin.gr.rules.Grammar
import me.vzhilin.gr.smt.Cells
import kotlin.test.Test
import kotlin.test.assertTrue

class DerivationExample {
    private fun sampleCells(): Triple<Grammar, String, Cells> {
        val g = simpleGrammar()
        val input = "λx.xλy.y"
        val cols = input.length
        val rows = 7

        val cells = Cells(rows, cols)
        fun setRuleId(rowId: Int, vararg rules: String) {
            cells.setRuleId(rowId, rules.map { g[it].id }.toTypedArray())
        }

        fun setGroupId(rowId: Int, vararg ids: Int)    = cells.setGroupId(rowId, ids.toTypedArray())
        fun setSubGroupId(rowId: Int, vararg ids: Int) = cells.setSubGroupId(rowId, ids.toTypedArray())
        fun setIndex(rowId: Int, vararg ids: Int)      = cells.setIndex(rowId, ids.toTypedArray())
        fun setProdType(rowId: Int, vararg ids: Int)   = cells.setProductionTypeId(rowId, ids.toTypedArray())

        setRuleId(6,    "T",    "T",    "T",    "T",    "T",    "T",    "T",    "T")
        setRuleId(5,  "APP",  "APP",  "APP",  "APP",  "APP",  "APP",  "APP",  "APP")
        setRuleId(4,    "T",    "T",    "T",    "T",    "T",    "T",    "T",    "T")
        setRuleId(3, "ABST", "ABST", "ABST", "ABST", "ABST", "ABST", "ABST", "ABST")
        setRuleId(2,    "λ",    "V",    ".",    "T",    "λ",    "V",    ".",    "T")
        setRuleId(1,    "λ",    "V",    ".",    "V",    "λ",    "V",    ".",    "V")
        setRuleId(0,    "λ",    "x",    ".",    "x",    "λ",    "y",    ".",    "y")

        setGroupId(6,     0,      0,      0,      0,      0,      0,      0,      0)
        setGroupId(5,     0,      0,      0,      0,      0,      0,      0,      0)
        setGroupId(4,     0,      0,      0,      0,      1,      1,      1,      1)
        setGroupId(3,     0,      0,      0,      0,      1,      1,      1,      1)
        setGroupId(2,     0,      1,      2,      3,      4,      5,      6,      7)
        setGroupId(1,     0,      1,      2,      3,      4,      5,      6,      7)
        setGroupId(0,     0,      1,      2,      3,      4,      5,      6,      7)

        setSubGroupId(6,  0,      0,      0,      0,      0,      0,      0,      0)
        setSubGroupId(5,  0,      0,      0,      0,      1,      1,      1,      1)
        setSubGroupId(4,  0,      0,      0,      0,      0,      0,      0,      0)
        setSubGroupId(3,  0,      1,      2,      3,      0,      1,      2,      3)
        setSubGroupId(2,  0,      0,      0,      0,      0,      0,      0,      0)
        setSubGroupId(1,  0,      0,      0,      0,      0,      0,      0,      0)
        setSubGroupId(0,  0,      0,      0,      0,      0,      0,      0,      0)

        // 0: bypass, 1: sum, 2: prod
        setProdType(6,    1,      1,      1,      1,      1,      1,      1,      1)
        setProdType(5,    2,      2,      2,      2,      2,      2,      2,      2)
        setProdType(4,    1,      1,      1,      1,      1,      1,      1,      1)
        setProdType(3,    2,      2,      2,      2,      2,      2,      2,      2)
        setProdType(2,    0,      0,      0,      1,      0,      0,      0,      1)
        setProdType(1,    0,      1,      0,      1,      0,      1,      0,      1)
        setProdType(0,    0,      0,      0,      0,      0,      0,      0,      0)

        setIndex(6,       0,      1,      2,      3,      4,      5,      6,      7)
        setIndex(5,       0,      1,      2,      3,      4,      5,      6,      7)
        setIndex(4,       0,      1,      2,      3,      0,      1,      2,      3)
        setIndex(3,       0,      1,      2,      3,      0,      1,      2,      3)
        setIndex(2,       0,      0,      0,      0,      0,      0,      0,      0)
        setIndex(1,       0,      0,      0,      0,      0,      0,      0,      0)
        setIndex(0,       0,      0,      0,      0,      0,      0,      0,      0)
        return Triple(g, input, cells)
    }

    @Test
    fun test() {
        val (g, input, cells) = sampleCells()
        val constraints = allConstraints(g, cells.rows, input, goal)
        assertTrue(cells.validate(*constraints.toTypedArray()))
    }
}