package me.vzhilin.gr.rules

import kotlin.IllegalStateException

sealed class Rule {
    abstract val name: String
}

sealed class Atom

data class Sum(override val name: String, val args: List<Atom>): Rule() {
    init {
        if (args.size < 2) throw IllegalStateException()
    }
    constructor(name: String, vararg refs: String): this(name, refs.map(::Ref))

    override fun toString(): String {
        return "$name → ${args.joinToString(" ")}"
    }
}

data class Prod(override val name: String, val args: List<Atom>): Rule() {
    init {
        if (args.size < 2) throw IllegalStateException()
    }
    constructor(name: String, vararg refs: String): this(name, refs.map(::Ref))

    override fun toString(): String {
        return "$name → ${args.joinToString(" | ")}"
    }
}

data class Term(val value: Char): Atom() {
    override fun toString(): String {
        return "'$value'"
    }
}

// TODO get rid from Ref
//data class Ref(val nonTermName: String): Atom() {
//    override fun toString(): String {
//        return nonTermName
//    }
//}