package me.vzhilin.gr.rules

import kotlin.IllegalStateException

data class DerivationLimits(val min: Int, val max: Int)

class ComputeLimits(private val g: Grammar) {
    private val revCache = mutableMapOf<Rule, List<Rule>>()

    private fun reverse(r: Rule): List<Rule> {
        return revCache.computeIfAbsent(r) {
            g.prods.filter { it.components.contains(r) } + g.sums.filter { it.components.contains(r) }
        }
    }

    fun computeTreeHeights(goal: NonTerm, inputLength: Int): DerivationLimits {
        val lastRow    = mutableMapOf<Rule, MutableSet<Int>>()
        val curRow     = mutableMapOf<Rule, MutableSet<Int>>()
        val allSymbols = mutableMapOf<Rule, MutableSet<Int>>()

        var minHeight: Int? = null
        var maxHeight: Int? = null

        fun addToCurRow(rule: Rule, vararg sizes: Int) {
            curRow.computeIfAbsent(rule) { mutableSetOf() }.addAll(sizes.toList())
        }

        fun addToAllSymbols(rule: Rule, vararg sizes: Int) {
            allSymbols.computeIfAbsent(rule) { mutableSetOf() }.addAll(sizes.toList())
        }

        fun sizes(r: Rule) =
            if (r is Term) {
                setOf(1)
            } else {
                (lastRow[r] ?: emptySet()) + (allSymbols[r] ?: emptySet())
            }

        for (term in g.terms) {
            lastRow[term] = mutableSetOf(1)
        }

        var treeDepth = 1
        while (!lastRow.contains(goal) || lastRow[goal]!!.min() <= inputLength) {
            if (lastRow.isEmpty()) {
                throw AssertionError("last row should never be empty")
            }
            for ((rule, sizes) in lastRow) {
                val rr = reverse(rule)
                for (revRule in rr) {
                    when (revRule) {
                        is Prod -> {
                            val components = revRule.components
                            if (components.all { it is Term || lastRow.contains(it) || allSymbols.contains(it) }) {
                                var ruleVisited = false
                                val minProdSize = components.sumOf { component ->
                                    if (component == rule && !ruleVisited) {
                                        ruleVisited = true
                                        sizes.min()
                                    } else {
                                        sizes(component).min()
                                    }
                                }
                                val maxProdSize = components.sumOf {
                                    component -> sizes(component).max()
                                }
                                addToCurRow(revRule, minProdSize, maxProdSize)
                            }
                        }

                        is Sum -> addToCurRow(revRule, sizes.min(), sizes.max())

                        is Term -> throw IllegalStateException("not expected here: '$revRule'")
                    }
                }
            }

            for ((rule, sizes) in curRow) {
                val min = sizes.min()
                val max = sizes.max()
                if (rule == goal && minHeight == null && max >= inputLength) {
                    minHeight = treeDepth
                }

                if (rule == goal && min <= inputLength) {
                    maxHeight = treeDepth
                }
                addToAllSymbols(rule, min, max)
            }
            lastRow.clear()
            lastRow.putAll(curRow)
            curRow.clear()
            ++treeDepth
        }

        return DerivationLimits(minHeight!!, maxHeight!!)
    }
}