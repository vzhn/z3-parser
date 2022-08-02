package me.vzhilin.gr.constraints

import me.vzhilin.gr.rules.Grammar

data class Config(
    val grammar: Grammar,
    val rows: Int,
    val columns: Int
)