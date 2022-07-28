package me.vzhilin.gr

fun simpleGrammar(): Grammar {
    val x = Term("X", 'x')
    val y = Term("Y", 'y')
    val dot = Term("DOT", '.')
    val lambda = Term("LAMBDA", 'λ')

    /* T = V | APP | ABST */
    val t = Sum("T", "V", "APP", "ABST")

    /* V = A | B | C | D */
    val v = Sum("V", "X", "Y")

    /* APP = T T */
    val app = Prod("APP", "T", "T")

    /* ABST = λV.T */
    val abst = Prod("ABST", "LAMBDA", "V", "DOT", "T")

    return Grammar(x, y, dot, lambda, t, v, app, abst)
}