package me.vzhilin.gr.next.constraints

import me.vzhilin.gr.next.Environment
import me.vzhilin.gr.simpleGrammar
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConstraintTests: AbstractConstraintTests() {
    private val env1x4 = object : Environment {
        override val grammar = simpleGrammar()
        override val rows = 1
        override val columns = 4
    }

    private val env2x2 = object : Environment {
        override val grammar = simpleGrammar()
        override val rows = 2
        override val columns = 2
    }

    @Test
    fun `basic range constraints`() {
        val env = object : Environment {
            override val grammar = simpleGrammar()
            override val rows = 1
            override val columns = 4
        }

        assertTrue(
            Matrix(env).also {
                it.set(MatrixCell::index, 0, 1, 2, 3)
            }.validate(BasicRanges)
        )

        assertFalse(
            Matrix(env).also {
                it.set(MatrixCell::index, 1, 2, 3, 4)
            }.validate(BasicRanges)
        )

        assertTrue(
            Matrix(env).also {
                it.set(MatrixCell::index, 0, 1, 0, 1)
            }.validate(BasicRanges)
        )
    }

    @Test
    fun `start fields eq zero`() {
        assertTrue(
            Matrix(env1x4).also {
                it.set(MatrixCell::index, 0, 1, 0, 1)
                it.set(MatrixCell::groupId, 0, 0, 1, 1)
                it.set(MatrixCell::subGroupId, 0, 0, 0, 0)
            }.validate(StartFields)
        )

        assertFalse(
            Matrix(env1x4).also {
                it.set(MatrixCell::index, 1, 2, 3, 4)
            }.validate(StartFields)
        )

        assertFalse(
            Matrix(env1x4).also {
                it.set(MatrixCell::groupId, 1, 2, 3, 4)
            }.validate(StartFields)
        )

        assertFalse(
            Matrix(env1x4).also {
                it.set(MatrixCell::subGroupId, 1, 2, 3, 4)
            }.validate(StartFields)
        )
    }

    @Test
    fun `groupId constraints`() {
        assertTrue(
            Matrix(env1x4).also {
                it.set(MatrixCell::groupId, 0, 1, 1, 1)
            }.validate(AdjGroupId)
        )

        assertFalse(
            Matrix(env1x4).also {
                it.set(MatrixCell::groupId, 0, 1, 3, 3)
            }.validate(AdjGroupId)
        )
    }

    @Test
    fun `subGroupId constraints for horizontally adjacent cells`() {
        assertTrue(
            Matrix(env1x4).also {
                it.set(MatrixCell::groupId, 0, 1, 1, 1)
                it.set(MatrixCell::subGroupId, 0, 0, 1, 2)
            }.validate(AdjSubGroupId)
        )

        assertTrue(
            Matrix(env1x4).also {
                it.set(MatrixCell::groupId, 0, 1, 1, 1)
                it.set(MatrixCell::subGroupId, 0, 0, 0, 1)
            }.validate(AdjSubGroupId)
        )

        assertTrue(
            Matrix(env1x4).also {
                it.set(MatrixCell::groupId, 0, 1, 1, 1)
                it.set(MatrixCell::subGroupId, 0, 0, 1, 1)
            }.validate(AdjSubGroupId)
        )

        assertFalse(
            Matrix(env1x4).also {
                it.set(MatrixCell::groupId, 0, 1, 1, 1)
                it.set(MatrixCell::subGroupId, 0, 1, 2, 3)
            }.validate(AdjSubGroupId)
        )
    }

    @Test
    fun `adjacent cell index`() {
        assertTrue(
            Matrix(env1x4).also {
                it.set(MatrixCell::groupId, 0, 1, 1, 1)
                it.set(MatrixCell::index, 0, 0, 1, 2)
            }.validate(AdjCellIndex)
        )

        assertFalse(
            Matrix(env1x4).also {
                it.set(MatrixCell::groupId, 0, 1, 1, 1)
                it.set(MatrixCell::index, 0, 0, 1, 1)
            }.validate(AdjCellIndex)
        )
    }

    @Test
    fun `dont divide group`() {
        assertFalse(Matrix(env2x2).also {
            it.set(MatrixCell::index,
                0, 1,
                0, 0
            )
        }.validate(DontDivideGroup))

        assert(Matrix(env2x2).also {
            it.set(MatrixCell::index,
                0, 1,
                0, 1
            )
        }.validate(DontDivideGroup))
    }
}