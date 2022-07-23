package me.vzhilin.gr

import kotlin.test.Test

class SequencesTest {
    @Test
    fun test() {
        val rows = 4
        val cols = 4

        fun field(rn: Int, cn: Int) = "${rn}_${cn}"

        (0 until rows).forEach { row ->
            (0 until cols).forEachAdjacentPair { lhs, rhs ->
                val left = field(row, lhs)
                val right = field(row, rhs)

                val commands = """
                    cond1 = $right.line = $left.line
                    cond2 = $right.line = $left.line + 1
                    cond3 = 
                    
                """.trimIndent()
            }
        }


    }
}