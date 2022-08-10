package me.vzhilin.gr.parser

import me.vzhilin.gr.parser.ProductionTypeId.Companion.BYPASS
import me.vzhilin.gr.parser.ProductionTypeId.Companion.PROD
import me.vzhilin.gr.parser.ProductionTypeId.Companion.SUM
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val g = simpleGrammar()

class ConstraintTests {
    @Test
    fun `basic range constraints`() {
        assertTrue(
            Cells(1, 4).also {
                it.setIndex(0, arrayOf(0, 1, 2, 3))
            }.validate(BasicRanges(g, 1, 4))
        )

        assertFalse(
            Cells(1, 4).also {
                it.setIndex(0, arrayOf(1, 2, 3, 4))
            }.validate(BasicRanges(g, 1, 4))
        )

        assertTrue(
            Cells(1, 4).also {
                it.setIndex(0, arrayOf(0, 1, 0, 1))
            }.validate(BasicRanges(g, 1, 4))
        )
    }

    @Test
    fun `start fields eq zero`() {
        assertTrue(
            Cells(1, 4).also {
                it.setIndex(0, arrayOf(0, 1, 0, 1))
                it.setIndex(0, arrayOf(0, 0, 1, 1))
                it.setIndex(0, arrayOf(0, 0, 0, 0))
            }.validate(StartFields)
        )

        assertFalse(
            Cells(1, 4).also {
                it.setIndex(0, arrayOf(1, 2, 3, 4))
            }.validate(StartFields)
        )

        assertFalse(
            Cells(1, 4).also {
                it.setGroupId(0, arrayOf(1, 2, 3, 4))
            }.validate(StartFields)
        )

        assertFalse(
            Cells(1, 4).also {
                it.setSubGroupId(0, arrayOf(1, 2, 3, 4))
            }.validate(StartFields)
        )
    }

    @Test
    fun `groupId constraints`() {
        assertTrue(
            Cells(1, 4).also {
                it.setGroupId(0, arrayOf(0, 1, 1, 1))
            }.validate(AdjGroupId)
        )

        assertFalse(
            Cells(1, 4).also {
                it.setGroupId(0, arrayOf(0, 1, 3, 3))
            }.validate(AdjGroupId)
        )
    }

    @Test
    fun `subGroupId constraints for horizontally adjacent cells`() {
        assertTrue(
            Cells(1, 4).also {
                it.setGroupId(0, arrayOf(0, 1, 1, 1))
                it.setSubGroupId(0, arrayOf(0, 0, 1, 2))
            }.validate(AdjSubGroupId)
        )

        assertTrue(
            Cells(1, 4).also {
                it.setGroupId(0, arrayOf(0, 1, 1, 1))
                it.setSubGroupId(0, arrayOf(0, 0, 0, 1))
            }.validate(AdjSubGroupId)
        )

        assertTrue(
            Cells(1, 4).also {
                it.setGroupId(0, arrayOf(0, 1, 1, 1))
                it.setSubGroupId(0, arrayOf(0, 0, 1, 1))
            }.validate(AdjSubGroupId)
        )

        assertFalse(
            Cells(1, 4).also {
                it.setGroupId(0, arrayOf(0, 1, 1, 1))
                it.setSubGroupId(0, arrayOf(0, 1, 2, 3))
            }.validate(AdjSubGroupId)
        )
    }

    @Test
    fun `adjacent cell index`() {
        assertTrue(
            Cells(1, 4).also {
                it.setGroupId(0, arrayOf(0, 1, 1, 1))
                it.setIndex(0, arrayOf(0, 0, 1, 2))
            }.validate(AdjCellIndex)
        )

        assertFalse(
            Cells(1, 4).also {
                it.setGroupId(0, arrayOf(0, 1, 1, 1))
                it.setIndex(0, arrayOf(0, 0, 1, 1))
            }.validate(AdjCellIndex)
        )
    }

    @Test
    fun `dont divide group`() {
        assertFalse(Cells(2, 2).also {
            it.setIndex(1, arrayOf(0, 0))
            it.setIndex(0, arrayOf(0, 1))
        }.validate(DontDivideGroup))

        assert(Cells(2, 2).also {
            it.setIndex(1, arrayOf(0, 1))
            it.setIndex(0, arrayOf(0, 1))
        }.validate(DontDivideGroup))

        assert(Cells(2, 2).also {
            it.setIndex(1, arrayOf(0, 1))
            it.setIndex(0, arrayOf(0, 0))
        }.validate(DontDivideGroup))
    }

    @Test
    fun `same groupId impl same ruleId`() {
        assert(Cells(1, 4).also {
            it.setGroupId(0, arrayOf(0, 0, 1, 1))
            it.setRuleId(0, arrayOf(2, 2, 3, 3))
        }.validate(SameGroupIdImplSameRuleId))

        assert(Cells(1, 4).also {
            it.setGroupId(0, arrayOf(0, 0, 1, 1))
            it.setRuleId(0, arrayOf(2, 2, 2, 2))
        }.validate(SameGroupIdImplSameRuleId))

        assertFalse(Cells(1, 4).also {
            it.setGroupId(0, arrayOf(0, 0, 1, 1))
            it.setRuleId(0, arrayOf(2, 2, 3, 2))
        }.validate(SameGroupIdImplSameRuleId))
    }

    @Test
    fun `same RuleId impl same RuleTypeId`() {
        assert(Cells(1, 4).also {
            it.setRuleId(0, arrayOf(0, 0, 1, 1))
            it.setProdTypeId(0, arrayOf(2, 2, 3, 3))
        }.validate(SameRuleIdImplSameRuleType))

        assert(Cells(1, 4).also {
            it.setRuleId(0, arrayOf(0, 0, 1, 1))
            it.setProdTypeId(0, arrayOf(2, 2, 2, 2))
        }.validate(SameRuleIdImplSameRuleType))

        assertFalse(Cells(1, 4).also {
            it.setRuleId(0, arrayOf(0, 0, 1, 1))
            it.setProdTypeId(0, arrayOf(2, 2, 3, 2))
        }.validate(SameRuleIdImplSameRuleType))
    }

    @Test
    fun `SubGroups could be not-zero only in production rules`() {
        assert(Cells(1, 4).also {
            it.setProdTypeId(0, arrayOf(PROD.n, PROD.n, PROD.n, PROD.n))
            it.setSubGroupId(0, arrayOf(0, 1, 2, 3))
        }.validate(SubGroupIdAlwaysZeroForNonProductionRules))

        assertTrue(Cells(1, 4).also {
            it.setProdTypeId(0, arrayOf(SUM.n, SUM.n, SUM.n, SUM.n))
            it.setSubGroupId(0, arrayOf(0, 0, 0, 0))
        }.validate(SubGroupIdAlwaysZeroForNonProductionRules))

        assertTrue(Cells(1, 4).also {
            it.setProdTypeId(0, arrayOf(BYPASS.n, BYPASS.n, BYPASS.n, BYPASS.n))
            it.setSubGroupId(0, arrayOf(0, 0, 0, 0))
        }.validate(SubGroupIdAlwaysZeroForNonProductionRules))

        assertFalse(Cells(1, 4).also {
            it.setProdTypeId(0, arrayOf(SUM.n, SUM.n, SUM.n, SUM.n))
            it.setSubGroupId(0, arrayOf(0, 1, 2, 3))
        }.validate(SubGroupIdAlwaysZeroForNonProductionRules))

        assertFalse(Cells(1, 4).also {
            it.setProdTypeId(0, arrayOf(BYPASS.n, BYPASS.n, BYPASS.n, BYPASS.n))
            it.setSubGroupId(0, arrayOf(0, 1, 2, 3))
        }.validate(SubGroupIdAlwaysZeroForNonProductionRules))
    }

    @Test
    fun `for product rule different subGroupIds iff different groupIds in bottom cells`() {
        assert(Cells(2, 2).also {
            it.setProdTypeId(1, arrayOf(BYPASS.n, BYPASS.n))
            it.setProdTypeId(0, arrayOf(PROD.n, PROD.n))

            it.setGroupId(1, arrayOf(0, 0))
            it.setGroupId(0, arrayOf(0, 0))

            it.setSubGroupId(1, arrayOf(1, 1))
            it.setSubGroupId(0, arrayOf(0, 0))
        }.validate(DiffSubGroupIdIffDiffGroupId))

        assertFalse(Cells(2, 2).also {
            it.setProdTypeId(1, arrayOf(PROD.n, PROD.n))
            it.setProdTypeId(0, arrayOf(BYPASS.n, BYPASS.n))

            it.setGroupId(1, arrayOf(1, 1))
            it.setGroupId(0, arrayOf(0, 0))

            it.setSubGroupId(1, arrayOf(1, 0))
            it.setSubGroupId(0, arrayOf(0, 0))
        }.validate(DiffSubGroupIdIffDiffGroupId))
    }
}