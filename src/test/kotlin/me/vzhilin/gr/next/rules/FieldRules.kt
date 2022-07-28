package me.vzhilin.gr.next.rules

import me.vzhilin.gr.next.CellHandler
import me.vzhilin.gr.next.ColumnHandler
import me.vzhilin.gr.next.HorizHandler
import me.vzhilin.gr.next.RowHandler
import me.vzhilin.gr.next.VertHandler

interface FieldRules<C, E> {
    val horizHandlers: List<HorizHandler<C, E>>
    val vertHandlers: List<VertHandler<C, E>>
    val rowHandlers: List<RowHandler<C, E>>
    val columnHandler: List<ColumnHandler<C, E>>
    val cellHandlers: List<CellHandler<C, E>>
}