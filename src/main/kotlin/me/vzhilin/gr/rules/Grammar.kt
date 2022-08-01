package me.vzhilin.gr.rules

class Grammar(private val rules: List<Rule>) {
    val size get() = rules.size
    val terms get() = rules.filterIsInstance<Term>()
    val sums get() = rules.filterIsInstance<Sum>()
    val prods get() = rules.filterIsInstance<Prod>()

    fun get(name: String): Rule {
        return rules.first { it.name == name }
    }

    fun getTerm(ch: Char): Term {
        return get("'$ch'") as Term
    }

    fun rule(ruleId: Int): Rule {
        return rules.first { it.id == ruleId }
    }

    companion object {
        fun of(vararg lines: String): Grammar {
            return parse(*lines)
        }
    }
}

sealed class Rule {
    abstract val id: Int
    abstract val name: String
}

data class Term(
    override val id: Int,
    val ch: Char
): Rule() {
    override val name = ch.toString()
}

sealed class NonTerm: Rule() {
    abstract val components: List<Rule>
}

data class Prod(
    override val id: Int,
    override val name: String,
    override val components: List<Rule>
): NonTerm()

data class Sum(
    override val id: Int,
    override val name: String,
    override val components: List<Rule>
): NonTerm()

private fun parse(vararg lines: String): Grammar {
    val ruleToId = mutableMapOf<String, Int>()
    val idToRule = mutableMapOf<Int, String>()
    val products = mutableMapOf<Int, List<Int>>()
    val sums     = mutableMapOf<Int, List<Int>>()
    val terms    = mutableSetOf<Int>()

    fun index(rule: String): Int {
        if (rule.startsWith("'") && (rule.length == 3 || rule.last() != '\'')) {
            throw IllegalArgumentException("expected '$rule' one-char term ")
        }

        val ruleId = ruleToId.computeIfAbsent(rule) { ruleToId.size }
        if (rule.startsWith("'")) {
            terms.add(ruleId)
        }
        return ruleId
    }
    lines.forEach {
        val (nonTerm, rule) = it.split("=")
        if (ruleToId.contains(nonTerm)) {
            throw IllegalArgumentException("rule '$nonTerm' is defined twice")
        }
        val id = index(nonTerm)
        if (rule.contains("|")) {
            sums[id] = rule.split('|').map(::index)
        } else {
            products[id] = rule.split(' ').map(::index)
        }
    }

    val idToSymbols = mutableMapOf<Int, Rule>()
    fun resolve(id: Int): Rule {
        if (!idToSymbols.contains(id)) {
            when {
                products.contains(id) -> {
                    val components = mutableListOf<Rule>()
                    idToSymbols[id] = Prod(id, idToRule[id]!!, components)
                    components.addAll(products[id]!!.map(::resolve))
                }
                sums.contains(id) -> {
                    val components = mutableListOf<Rule>()
                    idToSymbols[id] = Sum(id, idToRule[id]!!, components)
                    components.addAll(products[id]!!.map(::resolve))
                }
                terms.contains(id) -> {
                    val ch = idToRule[id]!!
                    if (ch.length != 1) {
                        throw IllegalArgumentException("expected term rule to be size=1: '$ch'")
                    }
                    idToSymbols[id] = Term(id, ch[0])
                }
                else -> throw IllegalArgumentException("not found: '$id'")
            }
        }

        return idToSymbols[id]!!
    }

    return Grammar(idToRule.keys.map(::resolve))
}