package me.vzhilin.gr

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context

class InputAssertions(
    val input: String,
    val grammar: Grammar,
    val cells: CellsContainer) {

    fun make(ctx: Context): List<BoolExpr> {
        if (input.length != cells.columns) {
            throw IllegalStateException()
        }

        val charToTerms = grammar.terms.groupBy { term -> term.value }
        return input.flatMapIndexed { index, ch ->
            val terms = charToTerms[ch] ?:
                throw IllegalStateException("rule not found for '$ch'")

            terms.map(grammar::id).map {
                ctx.mkEq(cells.rule(index), ctx.mkInt(it))
            }
        }
    }
}
