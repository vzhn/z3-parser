package me.vzhilin.gr.next.constraints

import me.vzhilin.gr.next.Cell
import me.vzhilin.gr.next.Environment
import me.vzhilin.gr.simpleGrammar

abstract class AbstractConstraintTests {

}

fun prod(rows: Int, columns: Int): List<Cell> {
    val pairs = mutableListOf<Cell>()
    for (rowId in 0 until rows) {
        for (colId in 0 until columns) {
            pairs.add(Cell(rowId, colId))
        }
    }
    return pairs
}