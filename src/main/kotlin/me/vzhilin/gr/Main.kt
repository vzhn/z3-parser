package me.vzhilin.gr

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import me.vzhilin.gr.rules.*
import java.io.File

fun main(argv: Array<String>) {
    val parser = ArgParser("smt-grammar")
    val grammarFilePath by parser.option(ArgType.String, shortName = "g", description = "Grammar file").required()
    val inputFilePath by parser.option(ArgType.String, shortName = "i", description = "Input file").required()
    parser.parse(argv)

    val g = readGrammar(grammarFilePath)
    val input = File(inputFilePath).readText().trim()
    val result = SMTParser(g, input, 4).parse()
    print(result)
}

fun print(result: SMTParsingResult) {
    fun asString(list: List<DerivationSymbol>): String {
        return list.joinToString("") { sym ->
            when (sym) {
                is NonTerminalDerivation -> "${sym.rule.name}(\'${sym.word}\')"
                is TerminalDerivation -> "'${sym.rule.ch}'"
            }
        }
    }
    val output = when (result) {
        SMTParsingResult.NoSolutions -> "no solutions"
        SMTParsingResult.NotEnoughRows -> "not enough rows"
        is SMTParsingResult.Solution -> {
            result.derivation.joinToString("\n") { step -> when (step) {
                    is DerivationStep.Middle -> {
                        val substitutions = step.substitutions.joinToString(" ") {
                            val name = it.first.name

                            val (first, last) = it.second.first to it.second.last
                            val range = if (first != last) {
                                "${first}:${last}"
                            } else {
                                "$first"
                            }
                            "${name}($range)"
                        }

                        "${asString(step.input)} # $substitutions"
                    }
                    is DerivationStep.Tail -> {
                        asString(step.input)
                    }
                }
            }
        }
    }

    println(output)
}

private fun readGrammar(input: String): Grammar {
    return Grammar.of(*File(input).useLines(block = Sequence<String>::toList).toTypedArray())
}