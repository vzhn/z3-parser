package me.vzhilin.gr.parser

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AllConstraintsTests {
    private fun sampleCells1(): Triple<Grammar, String, Cells> {
        val g = simpleGrammar()
        val input = "λx.xλy.y"
        val cells = Cells(7, input.length)
        with (cells) {
            setRuleId(6, g,    "T",    "T",    "T",    "T",    "T",    "T",    "T",    "T")
            setRuleId(5, g,  "APP",  "APP",  "APP",  "APP",  "APP",  "APP",  "APP",  "APP")
            setRuleId(4, g,    "T",    "T",    "T",    "T",    "T",    "T",    "T",    "T")
            setRuleId(3, g, "ABST", "ABST", "ABST", "ABST", "ABST", "ABST", "ABST", "ABST")
            setRuleId(2, g,    "λ",    "V",    ".",    "T",    "λ",    "V",    ".",    "T")
            setRuleId(1, g,    "λ",    "V",    ".",    "V",    "λ",    "V",    ".",    "V")
            setRuleId(0, g,    "λ",    "x",    ".",    "x",    "λ",    "y",    ".",    "y")

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
            setProdTypeId(6,    1,      1,      1,      1,      1,      1,      1,      1)
            setProdTypeId(5,    2,      2,      2,      2,      2,      2,      2,      2)
            setProdTypeId(4,    1,      1,      1,      1,      1,      1,      1,      1)
            setProdTypeId(3,    2,      2,      2,      2,      2,      2,      2,      2)
            setProdTypeId(2,    0,      0,      0,      1,      0,      0,      0,      1)
            setProdTypeId(1,    0,      1,      0,      1,      0,      1,      0,      1)
            setProdTypeId(0,    0,      0,      0,      0,      0,      0,      0,      0)

            setIndex(6,       0,      1,      2,      3,      4,      5,      6,      7)
            setIndex(5,       0,      1,      2,      3,      4,      5,      6,      7)
            setIndex(4,       0,      1,      2,      3,      0,      1,      2,      3)
            setIndex(3,       0,      1,      2,      3,      0,      1,      2,      3)
            setIndex(2,       0,      0,      0,      0,      0,      0,      0,      0)
            setIndex(1,       0,      0,      0,      0,      0,      0,      0,      0)
            setIndex(0,       0,      0,      0,      0,      0,      0,      0,      0)
        }

        return Triple(g, input, cells)
    }

    private fun sampleCells2(): Triple<Grammar, String, Cells> {
        val input = "λx.xλy.yy"
        val grammar = simpleGrammar()
        val cells = Cells(5, 9)
        with(cells) {
            setRuleId(4, grammar, "T", "T", "T", "T", "T", "T", "T", "T", "T")
            setRuleId(3, grammar, "ABST", "ABST", "ABST", "ABST", "ABST", "ABST", "ABST", "ABST", "ABST")
            setRuleId(2, grammar, "λ", "V", ".", "T", "λ", "V", ".", "T", "T")
            setRuleId(1, grammar, "λ", "x", ".", "V", "λ", "y", ".", "V", "V")
            setRuleId(0, grammar, "λ", "x", ".", "x", "λ", "y", ".", "y", "y")
            setGroupId(4, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setGroupId(3, 0, 0, 0, 0, 1, 1, 1, 1, 1)
            setGroupId(2, 0, 1, 2, 3, 4, 5, 6, 7, 7)
            setGroupId(1, 0, 1, 2, 3, 4, 5, 6, 7, 7)
            setGroupId(0, 0, 1, 2, 3, 4, 5, 6, 7, 8)
            setSubGroupId(4, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setSubGroupId(3, 0, 1, 2, 3, 0, 1, 2, 3, 3)
            setSubGroupId(2, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setSubGroupId(1, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setSubGroupId(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setProdTypeId(4, 1, 1, 1, 1, 1, 1, 1, 1, 1)
            setProdTypeId(3, 2, 2, 2, 2, 2, 2, 2, 2, 2)
            setProdTypeId(2, 0, 1, 0, 1, 0, 1, 0, 1, 1)
            setProdTypeId(1, 0, 0, 0, 1, 0, 0, 0, 1, 1)
            setProdTypeId(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setIndex(4, 0, 1, 2, 3, 4, 5, 6, 7, 8)
            setIndex(3, 0, 1, 2, 3, 0, 1, 2, 3, 4)
            setIndex(2, 0, 0, 0, 0, 0, 0, 0, 0, 1)
            setIndex(1, 0, 0, 0, 0, 0, 0, 0, 0, 1)
            setIndex(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        }
        return Triple(grammar, input, cells)
    }

    private fun sampleCells3(): Triple<Grammar, String, Cells> {
        val input = "λx.xλy.yy"
        val grammar = simpleGrammar()
        val cells = Cells(8, 9)
        with(cells) {
            setRuleId(7, grammar, "T", "T", "T", "T", "T", "T", "T", "T", "T")
            setRuleId(6, grammar, "ABST", "ABST", "ABST", "ABST", "ABST", "ABST", "ABST", "ABST", "ABST")
            setRuleId(5, grammar, "λ", "V", ".", "T", "T", "T", "T", "T", "T")
            setRuleId(4, grammar, "λ", "x", ".", "T", "T", "T", "T", "T", "T")
            setRuleId(3, grammar, "λ", "x", ".", "V", "ABST", "ABST", "ABST", "ABST", "V")
            setRuleId(2, grammar, "λ", "x", ".", "V", "λ", "V", ".", "T", "y")
            setRuleId(1, grammar, "λ", "x", ".", "x", "λ", "y", ".", "V", "y")
            setRuleId(0, grammar, "λ", "x", ".", "x", "λ", "y", ".", "y", "y")
            setGroupId(7, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setGroupId(6, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setGroupId(5, 0, 1, 2, 3, 3, 3, 3, 3, 3)
            setGroupId(4, 0, 1, 2, 3, 4, 4, 4, 4, 5)
            setGroupId(3, 0, 1, 2, 3, 4, 4, 4, 4, 5)
            setGroupId(2, 0, 1, 2, 3, 4, 5, 6, 7, 8)
            setGroupId(1, 0, 1, 2, 3, 4, 5, 6, 7, 8)
            setGroupId(0, 0, 1, 2, 3, 4, 5, 6, 7, 8)
            setSubGroupId(7, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setSubGroupId(6, 0, 1, 2, 3, 3, 3, 3, 3, 3)
            setSubGroupId(5, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setSubGroupId(4, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setSubGroupId(3, 0, 0, 0, 0, 0, 1, 2, 3, 0)
            setSubGroupId(2, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setSubGroupId(1, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setSubGroupId(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setProdTypeId(7, 1, 1, 1, 1, 1, 1, 1, 1, 1)
            setProdTypeId(6, 2, 2, 2, 2, 2, 2, 2, 2, 2)
            setProdTypeId(5, 0, 1, 0, 0, 0, 0, 0, 0, 0)
            setProdTypeId(4, 0, 0, 0, 1, 1, 1, 1, 1, 1)
            setProdTypeId(3, 0, 0, 0, 0, 2, 2, 2, 2, 1)
            setProdTypeId(2, 0, 0, 0, 1, 0, 1, 0, 1, 0)
            setProdTypeId(1, 0, 0, 0, 0, 0, 0, 0, 1, 0)
            setProdTypeId(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setIndex(7, 0, 1, 2, 3, 4, 5, 6, 7, 8)
            setIndex(6, 0, 1, 2, 3, 4, 5, 6, 7, 8)
            setIndex(5, 0, 0, 0, 0, 1, 2, 3, 4, 5)
            setIndex(4, 0, 0, 0, 0, 0, 1, 2, 3, 0)
            setIndex(3, 0, 0, 0, 0, 0, 1, 2, 3, 0)
            setIndex(2, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setIndex(1, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            setIndex(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        }
        return Triple(grammar, input, cells)
    }

    private fun sampleCells4(): Triple<Grammar, String, Cells> {
        val input = "yx"
        val grammar = simpleGrammar()
        val cells = Cells(5, 2)
        with (cells) {
            setRuleId(4, grammar, "T", "T")
            setRuleId(3, grammar, "APP", "APP")
            setRuleId(2, grammar, "T", "T")
            setRuleId(1, grammar, "V", "V")
            setRuleId(0, grammar, "y", "x")

            setGroupId(4, arrayOf(0, 0))
            setGroupId(3, arrayOf(0, 0))
            setGroupId(2, arrayOf(0, 1))
            setGroupId(1, arrayOf(0, 1))
            setGroupId(0, arrayOf(0, 1))

            setSubGroupId(4, arrayOf(0, 0))
            setSubGroupId(3, arrayOf(0, 1))
            setSubGroupId(2, arrayOf(0, 0))
            setSubGroupId(1, arrayOf(0, 0))
            setSubGroupId(0, arrayOf(0, 0))

            setProdTypeId(4, arrayOf(1, 1))
            setProdTypeId(3, arrayOf(2, 2))
            setProdTypeId(2, arrayOf(1, 1))
            setProdTypeId(1, arrayOf(1, 1))
            setProdTypeId(0, arrayOf(0, 0))

            setIndex(4, arrayOf(0, 1))
            setIndex(3, arrayOf(0, 1))
            setIndex(2, arrayOf(0, 0))
            setIndex(1, arrayOf(0, 0))
            setIndex(0, arrayOf(0, 0))
        }
        return Triple(grammar, input, cells)
    }

    @Test
    fun test1() {
        val (g, input, cells) = sampleCells1()
        val constraints = allConstraints(g, cells.rows, input)
        assertTrue(cells.validate(*constraints.toTypedArray()))
    }

    @Test
    fun test2() {
        val (g, input, cells) = sampleCells2()
        val constraints = allConstraints(g, cells.rows, input)
        assertFalse(cells.validate(*constraints.toTypedArray()))
    }

    @Test
    fun test3() {
        val (g, input, cells) = sampleCells3()
        val constraints = allConstraints(g, cells.rows, input)
        assertFalse(cells.validate(*constraints.toTypedArray()))
    }

    @Test
    fun test4() {
        val (g, input, cells) = sampleCells4()
        val constraints = allConstraints(g, cells.rows, input)
        assertTrue(cells.validate(*constraints.toTypedArray()))
    }
}