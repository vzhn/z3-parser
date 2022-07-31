package me.vzhilin.gr

import me.vzhilin.gr.rules.Grammar
import me.vzhilin.gr.rules.Prod
import me.vzhilin.gr.rules.Sum
import me.vzhilin.gr.rules.Term

fun simpleGrammar(): Grammar {
    val x = Term('x')
    val y = Term('y')
    val a = Term('a')
    val b = Term('b')
    val dot = Term('.')
    val lambda = Term('λ')

    /* T = V | APP | ABST */
    val t = Sum("T", "V", "APP", "ABST")

    /* V = X | Y | A | B */
    val v = Sum("V", "X", "Y", "A", "B")

    /* APP = T T */
    val app = Prod("APP", "T", "T")

    /* ABST = λV.T */
    val abst = Prod("ABST", "LAMBDA", "V", "DOT", "T")

    return Grammar(t, v, app, abst)
}