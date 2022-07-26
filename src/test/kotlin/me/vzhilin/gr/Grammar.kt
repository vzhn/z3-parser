package me.vzhilin.gr

class Grammar(vararg val allRules: Rule) {
    private val idToRule: Map<Int, Rule>
    private val ruleToId: Map<Rule, Int>

    private val nameToRule = allRules.associateBy { it.name }

    init {
        idToRule = mutableMapOf<Int, Rule>().also {
            allRules.forEachIndexed { index, rule -> it[index] = rule }
        }
        ruleToId = idToRule.map { (k, v) -> v to k }.toMap()
    }

    fun id(rule: Rule): Int {
        return ruleToId[rule]!!
    }

    fun resolve(ref: Ref): Rule {
        val name = ref.name
        return allRules.firstOrNull { it !is Ref && it.name == name } ?:
            throw IllegalArgumentException("Rule not found: '$name'")
    }

    fun rule(ruleId: Int): Rule {
        return idToRule[ruleId] ?: throw IllegalArgumentException("Rule not found for id: '$ruleId'")
    }

    val terms get(): List<Term> {
        return allRules.filterIsInstance<Term>()
    }

    val sums get(): List<Sum> {
        return allRules.filterIsInstance<Sum>()
    }

    val prods get(): List<Prod> {
        return allRules.filterIsInstance<Prod>()
    }
}