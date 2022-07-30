package me.vzhilin.gr

import kotlin.IllegalStateException

sealed class Rule {
    abstract val name: String
}

data class Sum(override val name: String, val args: List<Ref>): Rule() {
    init {
        if (args.size < 2) throw IllegalStateException()
    }
    constructor(name: String, vararg refs: String): this(name, refs.map(::Ref))

    override fun toString(): String {
        return "$name → ${args.joinToString(" ")}"
    }
}
data class Prod(override val name: String, val args: List<Ref>): Rule() {
    init {
        if (args.size < 2) throw IllegalStateException()
    }
    constructor(name: String, vararg refs: String): this(name, refs.map(::Ref))

    override fun toString(): String {
        return "$name → ${args.joinToString(" | ")}"
    }
}
data class Term(override val name: String, val value: Char): Rule() {
    override fun toString(): String {
        return "$name → '$value'"
    }
}
data class Ref(override val name: String): Rule() {
    override fun toString(): String {
        return name
    }
}