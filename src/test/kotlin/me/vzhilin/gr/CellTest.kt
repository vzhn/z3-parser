package me.vzhilin.gr

import kotlin.test.Test

class CellTest {
    val rulesList = listOf(
        Term("SPACE", " "),
        Term("DOT", "."),
        Term("X", "x"),
        Term("Y", "y"),
        Term("Z", "z"),
        Term("LAMBDA", "λ"),
        Term("DOT", "."),
        Sum("NAMES", "X", "Y", "Z"),

        Sum("T", "V", "APP", "ABST"),
        Prod("V", "NAMES"),
        Prod("APP", "T", "SPACE", "T"),
        Prod("ABST", "LAMBDA", "V", "DOT", "T")
    )
    private val rulesMap = rulesList.associateBy { it.name }

    @Test
    fun test() {
        fun ix(n: String) = rulesList.indexOf(rulesMap[n]!!)
        val input = "λx.y λx.y"
        val row0 = listOf(
            /*  'λ'   */ Cell(0, ix("LAMBDA"), 0),
            /*  'x'   */ Cell(1, ix("X"), 0),
            /*  '.'   */ Cell(2, ix("DOT"), 0),
            /*  'y'   */ Cell(3, ix("Y"), 0),
            /*  ' '   */ Cell(4, ix("SPACE"), 0),
            /*  'λ'   */ Cell(5, ix("LAMBDA"), 0),
            /*  'x'   */ Cell(6, ix("X"), 0),
            /*  '.'   */ Cell(7, ix("DOT"), 0),
            /*  'y'   */ Cell(8, ix("Y"), 0),
        )
        println(row0)
    }
}

// cell
// behaves differently for product and sum rules
data class ProductCell(
    // row number
    val i: Int,

    // rule number
    val ruleNumber: Int,

    // position in rule
    val pos: Int,

    // sub-rule number
    val subRuleNumber: Int,

    // position in sub-rule *
    val posInSubRule: Int
)

/**
 * Laws of cell
 *
 *
 *
 */

/**

 CELL
 1. Rule, for example ABST ⟶ 'λ' V '.' T
 2. Part of rule, for example V
 3. position in the part of rule
 4. terminal
 5. absolute index
 */
