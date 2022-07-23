package me.vzhilin.gr

import kotlin.math.PI
import kotlin.test.Test

class CellTest {
    /* terminals */
    private val A = Term("A", "a")
    private val B = Term("B", "b")
    private val C = Term("C", "c")
    private val D = Term("D", "d")
    private val DOT = Term("DOT", ".")
    private val LAMBDA = Term("LAMBDA", "λ")

    /* T = V | APP | ABST */
    private val T = Sum("T", "V", "APP", "ABST")

    /* V = A | B | C | D */
    private val V = Sum("V", "A", "B", "C", "D")

    /* APP = T T */
    private val APP = Prod("APP", "T", "T")

    /* ABST = λV.T */
    private val ABST = Prod("ABST", "LAMBDA", "V", "DOT", "T")

    private val allRules = listOf(A, B, C, D, DOT, LAMBDA, T, V, APP, ABST)
    private val nameToRule = allRules.associateBy { it.name }

    private fun type(it: Rule) = when (it) {
        is Term -> 1
        is Prod -> 2
        is Sum -> 3
        is Ref -> throw IllegalArgumentException()
    }

    private fun t(r: String): Int {
        return type(nameToRule[r]!!)
    }

    fun ix(r: Rule): Int {
        return allRules.indexOf(r)
    }

    @Test
    fun test() {
        val input = "λx.abcdd"
        prepareRules(input)
    }

    fun prepareRules(input: String) {
        val columnsNumber = input.length
        val rowsNumber = 7
        fun field(rn: Int, cn: Int) = "${rn}_${cn}"

        fun nonMonotonicallyIncreasing(rn: Int, field: String) {
            (0 until columnsNumber).forEachAdjacentPair { left, right ->
                val leftGroupId = "${field}_${field(rn, left)}"
                val rightGroupId = "${field}_${field(rn, right)}"
                println("$rightGroupId == $leftGroupId || $rightGroupId == $leftGroupId + 1")
            }
        }
        fun groupIdConstraints() {
            for (rn in 0..rowsNumber) {
                println("groupId_${field(rn, 0)} == 0")

                nonMonotonicallyIncreasing(rn, "groupId")
            }
        }
        fun posConstraints() {
            for (rn in 0..rowsNumber) {
                // in first column pos always equals zero
                println("pos_${field(rn, 0)} == 0")

                // in following columns pos equals zero when new group is started
                // otherwise pos equals prev pos + 1
                (1 until columnsNumber).forEachAdjacentPair { left, right ->
                    val leftGroupId = "groupId_${field(rn, left)}"
                    val rightGroupId = "groupId_${field(rn, right)}"
                    val leftPos = "pos_${field(rn, left)}"
                    val rightPos = "pos_${field(rn, right)}"

                    println("$leftGroupId == $rightGroupId => $rightPos = $leftPos + 1")
                    println("$leftGroupId != $rightGroupId => $rightPos = 0")
                }
            }
        }
        fun productConstraints() {
            for (rn in 0..rowsNumber) {
                (0 until columnsNumber).forEach { cn ->
                    val name = field(rn, cn)
                    println("isProduct_$name == true && pos_$name == 0 => child_$name == 0")
                }

                (0 until columnsNumber).forEachAdjacentPair { left, right ->
                    val leftName = field(rn, left)
                    val rightName = field(rn, right)
                    val bottomLeft = field(rn - 1, left)
                    val bottomRight = field(rn - 1, right)
                    val leftChild = "child_$leftName"
                    val rightChild = "child_$rightName"
                    val leftGroupId = "groupId_$leftName"
                    val rightGroupId = "groupId_$rightName"
                    val bottomLeftGroupId = "groupId_$bottomLeft"
                    val bottomRightGroupId = "groupId_$bottomRight"

                    println("isProduct_$leftName == true && $leftGroupId == $rightGroupId => $rightChild == $leftChild || $rightChild == $leftChild + 1")
                    println("isProduct_$leftName == true && $leftGroupId != $rightGroupId => $leftChild = lastChild_$leftName")
                    println("isProduct_$leftName == true && $leftGroupId == $rightGroupId && $leftChild == $rightChild => $bottomLeftGroupId == $bottomRightGroupId")
                    println("isProduct_$leftName == true && $leftGroupId == $rightGroupId && $leftChild + 1 == $rightChild => $bottomLeftGroupId + 1 == $bottomRightGroupId")
                }

                val last = field(rn, columnsNumber - 1)
                println("isProduct_$last == true => child_$last = lastChild_$last")
            }

            allRules.forEachIndexed { index, rule ->
                if (rule is Prod) {
                    for (rn in 0..rowsNumber) {
                        (0 until columnsNumber).forEach { cn ->
                            val name = field(rn, cn)
                            println("ruleId_$name == $index => isProduct_$name == true")
                            println("ruleId_$name == $index => lastChild_$name == ${rule.args.lastIndex}")
                        }
                    }
                }
            }
        }
        fun sumConstraints() {
            for (rn in 0..rowsNumber) {
                (0 until columnsNumber).forEach { cn ->
                    val name = field(rn, cn)
                    println("isSum_$name == true => child_$name >= 0 && child_$name <= lastChild_$name")
                }

                (0 until columnsNumber).forEachAdjacentPair { left, right ->
                    val leftName = field(rn, left)
                    val rightName = field(rn, right)
                    val bottomLeft = field(rn - 1, left)
                    val bottomRight = field(rn - 1, right)
                    val leftChild = "child_$leftName"
                    val rightChild = "child_$rightName"
                    val leftGroupId = "groupId_$leftName"
                    val rightGroupId = "groupId_$rightName"
                    val bottomLeftGroupId = "groupId_$bottomLeft"
                    val bottomRightGroupId = "groupId_$bottomRight"

                    println("isSum_$leftName == true && $leftGroupId == $rightGroupId => $rightChild == $leftChild && $bottomLeftGroupId == $bottomRightGroupId")
                }
            }
        }
        fun termConstraints() {

        }

        groupIdConstraints()
        posConstraints()
        productConstraints()
        sumConstraints()
    }
}

// cell
// behaves differently for product and sum rules
data class Cell(
    val row: Int,
    val col: Int,
    val rule: Rule,
    val groupId: Int,
    val pos: Int,
    val child: Int
)

fun IntRange.forEachAdjacentPair(f: (a: Int, b: Int) -> Unit) {
    for (i in first until last) {
        f(i, i + 1)
    }
}

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
