package me.vzhilin.gr

import kotlin.test.Test

class CellTest {
    private val rules = listOf(
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
    ).groupByTo(mutableMapOf()) { it.name }

    @Test
    fun test() {
        val input = "λx.y λx.y"

    }
}

/**

 CELL
 1. Rule, for example ABST ⟶ 'λ' V '.' T
 2. Part of rule, for example V
 3. position in the part of rule
 4. terminal
 5. absolute index
 */
