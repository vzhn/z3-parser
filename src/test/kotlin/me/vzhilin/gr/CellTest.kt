package me.vzhilin.gr

import kotlin.test.Test

class CellTest {
    val SPACE = Term("SPACE", " ")
    val DOT = Term("DOT", ".")
    val X = Term("X", "x")
    val Y = Term("Y", "y")
    val Z = Term("Z", "z")
    val LAMBDA = Term("LAMBDA", "λ")
    val NAMES = Sum("NAMES", "X", "Y", "Z")

    val T = Sum("T", "V", "APP", "ABST")
    val V = Prod("V", "NAMES")
    val APP = Prod("APP", "T", "SPACE", "T")
    val ABST = Prod("ABST", "LAMBDA", "V", "DOT", "T")

    val rulesList = listOf(
        SPACE, DOT, X, Y, Z, LAMBDA, DOT, NAMES, T, V, APP, ABST
    )
    private val rulesMap = rulesList.associateBy { it.name }
    private val rulesByType = rulesList.groupBy(::type)

    private fun type(it: Rule) = when (it) {
        is Term -> 1
        is Prod -> 2
        is Sum -> 3
        is Ref -> throw IllegalArgumentException()
    }

    private fun t(r: String): Int {
        return type(rulesMap[r]!!)
    }

    fun ix(n: String): Int {
        val r = rulesMap[n]!!
        val t = type(r)
        return rulesByType[t]!!.indexOf(r)
    }

    fun ix(r: Rule): Int {
        return rulesList.indexOf(r)
    }

    @Test
    fun test() {
        val input = "λx.x λy.y"
        val row0 = listOf(
            /*  'λ'   */ Cell(LAMBDA, 0, 0, 0),
            /*  'x'   */ Cell(X, 0, 0, 0),
            /*  '.'   */ Cell(DOT, 0, 0, 0),
            /*  'y'   */ Cell(Y, 0, 0, 0),
            /*  ' '   */ Cell(SPACE, 0, 0, 0),
            /*  'λ'   */ Cell(LAMBDA, 0, 0, 0),
            /*  'x'   */ Cell(X, 0, 0, 0),
            /*  '.'   */ Cell(DOT, 0, 0, 0),
            /*  'y'   */ Cell(Y, 0, 0, 0),
        )

        val row1 = listOf(
            /*  'λ'  */  Cell(LAMBDA, 0, 0, 0),
            /*   V   */  Cell(V, 0, 0, 0),
            /*  '.'  */  Cell(DOT, 0, 0, 0),
            /*   V   */  Cell(V, 0, 0, 0),
            /*  ' '   */ Cell(SPACE, 0, 0, 0),
            /*  'λ'  */  Cell(LAMBDA, 0, 0, 0),
            /*   V   */  Cell(V, 0, 0, 0),
            /*  '.'  */  Cell(DOT, 0, 0, 0),
            /*   V   */  Cell(V, 0, 0, 0),
        )

        val row2 = listOf(
            /*  'λ'  */  Cell(LAMBDA, 0, 0, 0),
            /*   V   */  Cell(T, 0, 0, 0),
            /*  '.'  */  Cell(DOT, 0, 0, 0),
            /*   T   */  Cell(V, 0, 0, 0),
            /*  ' '   */ Cell(SPACE, 0, 0, 0),
            /*  'λ'  */  Cell(LAMBDA, 0, 0, 0),
            /*   V   */  Cell(V, 0, 0, 0),
            /*  '.'  */  Cell(DOT, 0, 0, 0),
            /*   T   */  Cell(T, 0, 0, 0),
        )

        val row3 = listOf(
            /*  ABST  */  Cell(ABST, 0, 0, 0),
            /*  ABST  */  Cell(ABST, 1, 1, 0),
            /*  ABST  */  Cell(ABST, 2, 2, 0),
            /*  ABST  */  Cell(ABST, 3, 3, 0),
            /*  ' '   */  Cell(SPACE, 0, 0, 0),
            /*  ABST  */  Cell(ABST, 0, 0, 0),
            /*  ABST  */  Cell(ABST, 1, 1, 0),
            /*  ABST  */  Cell(ABST, 2, 2, 0),
            /*  ABST  */  Cell(ABST, 3, 3, 0),
        )

        val row4 = listOf(
            /*  T  */  Cell(T, 0, 0, 0),
            /*  T  */  Cell(T, 1, 0, 1),
            /*  T  */  Cell(T, 2, 0, 2),
            /*  T  */  Cell(T, 3, 0, 3),
            /*  T  */  Cell(T, 4, 1, 0),
            /*  T  */  Cell(T, 5, 2, 0),
            /*  T  */  Cell(T, 6, 2, 1),
            /*  T  */  Cell(T, 7, 2, 2),
            /*  T  */  Cell(T, 8, 2, 3),
        )
        val matrix = listOf(row0, row1, row2, row3, row4)
        checkMatrix(input, matrix)
    }

    fun checkMatrix(input: String, matrix: List<List<Cell>>) {
        matrix.forEachIndexed { columnIndex, cells ->
            cells.forEachIndexed { rowIndex, cell ->
                val rule = rulesList[cell.ruleNumber]
                val isFirstRow = rowIndex == 0
                val isLastRow = rowIndex == cells.lastIndex

                val upperCell by lazy { matrix[columnIndex][rowIndex - 1] }
                val leftCell by lazy { matrix[columnIndex - 1][rowIndex] }
                var cellOk = true
                if (columnIndex == 0) {
                    val char = input[rowIndex]
                    cellOk = cellOk && rule is Term
                        && cell.pos >= 0
                        && cell.pos <= rule.value.lastIndex
                        && rule.value[cell.pos] == char
                } else {
                    when (rule) {
                        is Prod -> {
                            if (isFirstRow) {
                                cellOk = cellOk && cell.subRuleNumber == 0
                            } else {
                                cellOk = cellOk && (
                                    cell.subRuleNumber == upperCell.subRuleNumber || cell.subRuleNumber == upperCell.subRuleNumber + 1
                                )
                            }
                            if (isLastRow) {
                                cellOk = cellOk && cell.subRuleNumber == rule.args.lastIndex
                            }

                            cellOk = cellOk && cell.subRuleNumber == leftCell.ruleNumber
                        }
                        is Sum -> TODO()
                        is Term -> TODO()
                        is Ref -> TODO()
                    }
                }
            }
        }
    }
}



// cell
// behaves differently for product and sum rules
data class Cell(
    val ruleNumber: Int,

    // position in the rule
    val pos: Int,

    // sub-rule number
    val subRuleNumber: Int,

    // position in sub-rule
    val posInSubRule: Int
)

/**
 * Laws of cell
 *
 *
 *
 */

/**
 Π
 CELL
 1. Rule, for example ABST ⟶ 'λ' V '.' T
 2. Part of rule, for example V
 3. position in the part of rule
 4. terminal
 5. absolute index
 */

// Terminal rule
// subRuleNumber == 0
// posInSubRule == 0

/*
    Cell modes:
Σ
    * rule
        * product
        * sum
        * terminal
    *  bypass
 */
