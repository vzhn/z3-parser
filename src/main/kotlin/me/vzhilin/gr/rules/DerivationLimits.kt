package me.vzhilin.gr.rules

import kotlin.IllegalStateException

class ComputeLimits(private val g: Grammar) {
    private val revCache = mutableMapOf<Rule, List<Rule>>()
    private val data = mutableMapOf<Rule, Set<Int>>()

    init {
        g.terms.forEach { term -> data[term] = setOf(1) }
    }

    private fun reverse(r: Rule): List<Rule> {
        return revCache.computeIfAbsent(r) {
            g.prods.filter { it.components.contains(r) } + g.sums.filter { it.components.contains(r) }
        }
    }

    private fun allSums(ints: List<Set<Int>>): Set<Int> {
        return ints.reduce { a: Set<Int>, b: Set<Int> ->
            val rs = mutableSetOf<Int>()
            for (m in a) {
                for (n in b) {
                    rs.add(m + n)
                }
            }
            rs
        }
    }

    fun computeRowNumbers(goal: NonTerm, input: String): Set<Int> {
        TODO()
    }


    fun next(): Map<Rule, Set<Int>> {
        val options: MutableMap<Rule, Set<Int>> = mutableMapOf()

        data.forEach { (rule, values) ->
            val parents = reverse(rule)
            parents.forEach { p ->
                when (p) {
                    is Prod -> {
                        if (p.components.all { data.contains(it) }) {
                            val list = p.components.map { data [it]!! }
                            val sums = allSums(list)
                            options.merge(p, sums) { a, b -> a + b }
                        }
                    }
                    is Sum -> {
                        options.merge(p, values) { a, b -> a + b }
                    }
                    is Term -> throw IllegalStateException("never happens")
                }
            }
        }

        val dataPrev = data.toMap()
        options.forEach { rule, vs ->
            data.merge(rule, vs) { a, b -> a + b }
        }

        val diffs = mutableMapOf<Rule, Set<Int>>()

        data.keys.forEach { rule ->
            val diff = data[rule]!! - (dataPrev[rule] ?: emptySet()).toSet()
            if (diff.isNotEmpty()) {
                diffs[rule] = diff
            }
        }
        return diffs
    }
}