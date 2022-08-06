package me.vzhilin.gr

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import me.vzhilin.gr.rules.*
import java.io.File

private val DefaultLimit = 3

fun main(argv: Array<String>) {
    val parser = ArgParser("smt-grammar")
    val grammarFilePath by parser.option(ArgType.String, shortName = "g", fullName = "grammar", description = "Grammar file").required()
    val inputFilePath by parser.option(ArgType.String, shortName = "i", fullName = "input", description = "Input file").required()
    val rows by parser.option(ArgType.Int, shortName = "r", fullName = "rows", description = "Rows").required()
    val derivationLimit by parser.option(ArgType.Int, shortName = "l", fullName = "limit", description = "derivation limit")
    val goal by parser.option(ArgType.String, fullName = "goal")
    val debug by parser.option(ArgType.Boolean, fullName = "debug")
    parser.parse(argv)

    if (derivationLimit == null) {
        println("Setting default derivation limit = $DefaultLimit")
        println()
    }

    val grammar = readGrammar(grammarFilePath)
    val input = File(inputFilePath).readText().trim()
    val smtParser = SMTParser(grammar, input, rows, goal?.let { grammar[it] as NonTerm })

    var solutionNumber = 1
    while (true) {
        val result = smtParser.parse()
        println("== derivation #$solutionNumber ==")
        if (result !is SMTParsingResult.Solution) {
            break
        } else {
            result.printDerivation()
            if (debug == true) {
                println("== cells #$solutionNumber ==")
                result.printCells()
            }
            println()
        }
        ++solutionNumber
        if (solutionNumber > (derivationLimit ?: DefaultLimit)) {
            println("Limit is reached")
            break
        }
    }
}

private fun readGrammar(input: String): Grammar {
    return Grammar.of(*File(input).useLines(block = Sequence<String>::toList).toTypedArray())
}