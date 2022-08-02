package me.vzhilin.gr

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import me.vzhilin.gr.rules.DerivationStep
import me.vzhilin.gr.rules.Grammar
import java.io.File

fun main(argv: Array<String>) {
    val parser = ArgParser("smt-grammar")
    val grammarFilePath by parser.option(ArgType.String, shortName = "g", description = "Grammar file").required()
    val inputFilePath by parser.option(ArgType.String, shortName = "i", description = "Input file").required()
    parser.parse(argv)

    val g = readGrammar(grammarFilePath)
    val input = File(inputFilePath).readText().trim()
    val derivation: List<DerivationStep> = SMTParser(g, input, 4).parse()
}

private fun readGrammar(input: String): Grammar {
    return Grammar.of(*File(input).useLines(block = Sequence<String>::toList).toTypedArray())
}