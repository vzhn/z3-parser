package me.vzhilin.gr.next.constraints

data class MatrixCell (
    val rowId: Int,
    var columnId: Int,
    var ruleId: Int = 0,
    var prodTypeId: Int = 0,
    var groupId: Int = 0,
    var subGroupId: Int = 0,
    var index: Int = 0
)