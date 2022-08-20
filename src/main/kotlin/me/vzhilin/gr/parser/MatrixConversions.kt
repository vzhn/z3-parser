package me.vzhilin.gr.model

import me.vzhilin.gr.parser.DerivationStep
import me.vzhilin.gr.parser.Grammar
import me.vzhilin.gr.parser.NonTerminalDerivation
import me.vzhilin.gr.parser.Prod
import me.vzhilin.gr.parser.Sum
import me.vzhilin.gr.parser.Term
import me.vzhilin.gr.parser.TerminalDerivation
import me.vzhilin.gr.parser.smt.Cells

fun Cells.toDerivation(grammar: Grammar): List<DerivationStep> {
    fun word(range: IntRange): String {
        return range.map { colId ->
            (grammar[getSymbolId(0, colId)] as Term).ch
        }.joinToString("")
    }
    fun getGroups(rowId: Int): Map<Int, List<Int>> {
        return (0 until cols).groupBy { colId -> getGroupId(rowId, colId) }
    }
    return (0 until rows).map { rowId ->
        val symbols = getGroups(rowId).mapValues { (_, vs) ->
            val symbolId = getOnly(vs.map { getSymbolId(rowId, it) })
            val range = IntRange(vs.first(), vs.last())
            when (val rule = grammar[symbolId]) {
                is Prod, is Sum -> NonTerminalDerivation(rule, word(range))
                is Term -> TerminalDerivation(rule)
            }
        }.values.toList()

        if (rowId == rows - 1) {
            DerivationStep.Tail(symbols)
        } else {
            val haveProductions = getGroups(rowId + 1).filterValues { values ->
                values.all { colId -> setProdTypeId(rowId + 1, colId) != 0 }
            }
            val substitutions = haveProductions.values.map { vs ->
                val rule = grammar[getSymbolId(rowId + 1, vs.first())]
                rule to getGroupId(rowId, vs.first())..getGroupId(rowId, vs.last())
            }
            DerivationStep.Middle(symbols, substitutions)
        }
    }
}

private fun getOnly(vs: List<Int>): Int {
    if (vs.distinct().size != 1) {
        throw IllegalArgumentException("not distinct collection")
    }
    return vs.first()
}

