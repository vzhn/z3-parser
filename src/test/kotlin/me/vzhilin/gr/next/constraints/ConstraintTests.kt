package me.vzhilin.gr.next.constraints

import me.vzhilin.gr.constraints.AdjCellIndex
import me.vzhilin.gr.constraints.AdjGroupId
import me.vzhilin.gr.constraints.AdjSubGroupId
import me.vzhilin.gr.constraints.BasicRanges
import me.vzhilin.gr.constraints.Config
import me.vzhilin.gr.constraints.DiffSubGroupIdIffDiffGroupId
import me.vzhilin.gr.constraints.DontDivideGroup
import me.vzhilin.gr.constraints.SameGroupIdImplSameRuleId
import me.vzhilin.gr.constraints.SameRuleIdImplSameRuleType
import me.vzhilin.gr.constraints.StartFields
import me.vzhilin.gr.constraints.SubGroupIdAlwaysZeroForNonProductionRules
import me.vzhilin.gr.constraints.exp.ProductionTypeId.Companion.BYPASS
import me.vzhilin.gr.constraints.exp.ProductionTypeId.Companion.PROD
import me.vzhilin.gr.constraints.exp.ProductionTypeId.Companion.SUM
import me.vzhilin.gr.model.Matrix
import me.vzhilin.gr.model.MatrixCell
import me.vzhilin.gr.simpleGrammar
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConstraintTests {
    private val env1x4 = Config(
        grammar = simpleGrammar(),
        rows = 1,
        columns = 4
    )

    private val env2x2 = Config(
        grammar = simpleGrammar(),
        rows = 2,
        columns = 2
    )

    @Test
    fun `basic range constraints`() {
        assertTrue(
            Matrix(env1x4).also {
                it.set(MatrixCell::index, 0, 1, 2, 3)
            }.validate(BasicRanges)
        )

        assertFalse(
            Matrix(env1x4).also {
                it.set(MatrixCell::index, 1, 2, 3, 4)
            }.validate(BasicRanges)
        )

        assertTrue(
            Matrix(env1x4).also {
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
            it.set(
                MatrixCell::index,
                0, 1,
                0, 0
            )
        }.validate(DontDivideGroup))

        assert(Matrix(env2x2).also {
            it.set(
                MatrixCell::index,
                0, 1,
                0, 1
            )
        }.validate(DontDivideGroup))
    }

    @Test
    fun `same groupId impl same ruleId`() {
        assert(Matrix(env1x4).also {
            it.set(MatrixCell::groupId, 0, 0, 1, 1)
            it.set(MatrixCell::ruleId, 2, 2, 3, 3)
        }.validate(SameGroupIdImplSameRuleId))

        assert(Matrix(env1x4).also {
            it.set(MatrixCell::groupId, 0, 0, 1, 1)
            it.set(MatrixCell::ruleId, 2, 2, 2, 2)
        }.validate(SameGroupIdImplSameRuleId))

        assertFalse(Matrix(env1x4).also {
            it.set(MatrixCell::groupId, 0, 0, 1, 1)
            it.set(MatrixCell::ruleId, 2, 2, 3, 2)
        }.validate(SameGroupIdImplSameRuleId))
    }

    @Test
    fun `same RuleId impl same RuleTypeId`() {
        assert(Matrix(env1x4).also {
            it.set(MatrixCell::ruleId, 0, 0, 1, 1)
            it.set(MatrixCell::prodTypeId, 2, 2, 3, 3)
        }.validate(SameRuleIdImplSameRuleType))

        assert(Matrix(env1x4).also {
            it.set(MatrixCell::ruleId, 0, 0, 1, 1)
            it.set(MatrixCell::prodTypeId, 2, 2, 2, 2)
        }.validate(SameRuleIdImplSameRuleType))

        assertFalse(Matrix(env1x4).also {
            it.set(MatrixCell::ruleId, 0, 0, 1, 1)
            it.set(MatrixCell::prodTypeId, 2, 2, 3, 2)
        }.validate(SameRuleIdImplSameRuleType))
    }

    @Test
    fun `SubGroups could be not-zero only in production rules`() {
        assert(Matrix(env1x4).also {
            it.set(MatrixCell::prodTypeId, PROD.n, PROD.n, PROD.n, PROD.n)
            it.set(MatrixCell::subGroupId, 0, 1, 2, 3)
        }.validate(SubGroupIdAlwaysZeroForNonProductionRules))

        assertTrue(Matrix(env1x4).also {
            it.set(MatrixCell::prodTypeId, SUM.n, SUM.n, SUM.n, SUM.n)
            it.set(MatrixCell::subGroupId, 0, 0, 0, 0)
        }.validate(SubGroupIdAlwaysZeroForNonProductionRules))

        assertTrue(Matrix(env1x4).also {
            it.set(MatrixCell::prodTypeId, BYPASS.n, BYPASS.n, BYPASS.n, BYPASS.n)
            it.set(MatrixCell::subGroupId, 0, 0, 0, 0)
        }.validate(SubGroupIdAlwaysZeroForNonProductionRules))

        assertFalse(Matrix(env1x4).also {
            it.set(MatrixCell::prodTypeId, SUM.n, SUM.n, SUM.n, SUM.n)
            it.set(MatrixCell::subGroupId, 0, 1, 2, 3)
        }.validate(SubGroupIdAlwaysZeroForNonProductionRules))

        assertFalse(Matrix(env1x4).also {
            it.set(MatrixCell::prodTypeId, BYPASS.n, BYPASS.n, BYPASS.n, BYPASS.n)
            it.set(MatrixCell::subGroupId, 0, 1, 2, 3)
        }.validate(SubGroupIdAlwaysZeroForNonProductionRules))
    }

    @Test
    fun `for product rule different subGroupIds iff different groupIds in bottom cells`() {
        assert(Matrix(env2x2).also {
            it.set(
                MatrixCell::prodTypeId,
                PROD.n, PROD.n,
                BYPASS.n, BYPASS.n)
            it.set(
                MatrixCell::groupId,
                0, 0,
                0, 0)
            it.set(
                MatrixCell::subGroupId,
                1, 1,
                0, 0)
        }.validate(DiffSubGroupIdIffDiffGroupId))

        assertFalse(Matrix(env2x2).also {
            it.set(
                MatrixCell::prodTypeId,
                PROD.n, PROD.n,
                BYPASS.n, BYPASS.n)
            it.set(
                MatrixCell::groupId,
                1, 1,
                0, 0)
            it.set(
                MatrixCell::subGroupId,
                1, 0,
                0, 0)
        }.validate(DiffSubGroupIdIffDiffGroupId))
    }
}