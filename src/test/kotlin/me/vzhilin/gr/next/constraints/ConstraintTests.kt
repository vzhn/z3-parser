package me.vzhilin.gr.next.constraints

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConstraintTests: AbstractConstraintTests() {
    @Test
    fun `basic range constraints`() {
        assertTrue(
            Matrix(env, 4, 1).also {
                it.set(MatrixCell::index,
                    0, 1, 2, 3
                )
            }.validate(BasicRanges)
        )
    }

    @Test
    fun `dont divide group`() {
        assertFalse(Matrix(env, 2, 2).also {
            it.set(MatrixCell::index,
                0, 1,
                0, 0
            )
        }.validate(DontDivideGroup))

        assert(Matrix(env, 2, 2).also {
            it.set(MatrixCell::index,
                0, 1,
                0, 1
            )
        }.validate(DontDivideGroup))
    }
}