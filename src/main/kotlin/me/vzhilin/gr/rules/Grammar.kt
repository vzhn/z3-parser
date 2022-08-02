package me.vzhilin.gr.rules

class Grammar(private val rules: List<Rule>) {
    val size get() = rules.size
    val terms get() = rules.filterIsInstance<Term>()
    val sums get() = rules.filterIsInstance<Sum>()
    val prods get() = rules.filterIsInstance<Prod>()

    operator fun get(ruleId: Int): Rule {
        return rules.first { it.id == ruleId }
    }

    operator fun get(name: String): Rule {
        return rules.firstOrNull { it.name == name } ?:
            throw IllegalArgumentException("rule not found: '$name'")
    }

    operator fun get(ch: Char): Term {
        return terms.firstOrNull { it.ch == ch } ?:
            throw IllegalArgumentException("term not found: '$ch'")
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
    override fun toString() = "'$ch'"
}

sealed class NonTerm: Rule() {
    abstract val components: List<Rule>
}

data class Prod(
    override val id: Int,
    override val name: String,
    override val components: List<Rule>
): NonTerm() {
    override fun toString() =
        "$name -> ${components.joinToString(" ", transform = Rule::name)}"
}

data class Sum(
    override val id: Int,
    override val name: String,
    override val components: List<Rule>
): NonTerm() {
    override fun toString(): String {
        return "$name -> ${components.joinToString(" | ", transform = Rule::name)}"
    }
}

private fun parse(vararg lines: String): Grammar {
    val ruleToId = mutableMapOf<String, Int>()
    val idToRule = mutableMapOf<Int, String>()
    val products = mutableMapOf<Int, List<Int>>()
    val sums     = mutableMapOf<Int, List<Int>>()
    val terms    = mutableSetOf<Int>()

    fun index(rule: String): Int {
        if (rule.startsWith(' ')) {
            throw IllegalArgumentException("rule could not start with ' ' (space)")
        }
        if (rule.endsWith(' ')) {
            throw IllegalArgumentException("rule could not end with ' ' (space)")
        }
        if (rule.startsWith("'") && (rule.length != 3 || rule.last() != '\'')) {
            throw IllegalArgumentException("expected '$rule' one-char term ")
        }

        val ruleId = ruleToId.computeIfAbsent(rule) {
            val id = ruleToId.size
            idToRule[id] = rule
            if (rule.startsWith("'")) {
                terms.add(id)
            }
            id
        }

        return ruleId
    }

    lines.forEach { it ->
        val (nonTerm, rule) = it.split("=").map(String::trim)
        val id = index(nonTerm)
        if (rule.contains("|")) {
            if (sums.contains(id)) {
                throw IllegalArgumentException("rule '$nonTerm' is defined twice")
            }
            sums[id] = rule.split('|').map(String::trim).map(::index)
        } else {
            if (products.contains(id)) {
                throw IllegalArgumentException("rule '$nonTerm' is defined twice")
            }
            products[id] = rule.split(' ').map(String::trim).flatMap { word ->
                if (word.startsWith("'")) {
                    if (!word.endsWith("'")) {
                        throw IllegalArgumentException("expected that '$rule' is terminal string")
                    } else {
                        word.substring(1, word.length - 1).map { ch -> "'$ch'" }.map(::index)
                    }
                } else {
                    listOf(index(word))
                }
            }
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
                    components.addAll(sums[id]!!.map(::resolve))
                }
                terms.contains(id) -> {
                    val ch = idToRule[id]!!
                    if (ch.length != 3 || !ch.startsWith('\'') || !ch.endsWith('\'')) {
                        throw IllegalArgumentException("expected term rule to be size=1: '$ch'")
                    }
                    idToSymbols[id] = Term(id, ch[1])
                }
                else -> throw IllegalArgumentException("not found: '$id'")
            }
        }
        return idToSymbols[id]!!
    }

    return Grammar(idToRule.keys.map(::resolve))
}