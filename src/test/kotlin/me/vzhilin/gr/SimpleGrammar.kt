package me.vzhilin.gr

fun simpleGrammar(): Grammar {
    val x = Term("X", 'x')
    val y = Term("Y", 'y')
    val a = Term("A", 'a')
    val b = Term("B", 'b')
    val dot = Term("DOT", '.')
    val lambda = Term("LAMBDA", 'λ')

    /* T = V | APP | ABST */
    val t = Sum("T", "V", "APP", "ABST")

    /* V = X | Y | A | B */
    val v = Sum("V", "X", "Y", "A", "B")

    /* APP = T T */
    val app = Prod("APP", "T", "T")

    /* ABST = λV.T */
    val abst = Prod("ABST", "LAMBDA", "V", "DOT", "T")

    return Grammar(x, y, a, b, dot, lambda, t, v, app, abst)
}