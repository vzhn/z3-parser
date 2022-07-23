package me.vzhilin.gr

import kotlin.test.Test

class SequencesTest {
    @Test
    fun test() {
        val rows = 4
        val seq =
            listOf(
                "line",  // Π[line]
                "row",   // Π[rowId] = a1 a2 a3 b1 b2
                "group", // Π[groupId] = [a1 a2 a3] [b1 b2]
                "childGroup", // Π[childGroupId] = [a11 a12 a13] [a21 a22 a23]
                "child" // Π[childId] = 0 1 2 3
            )

        // rowId
        // groupId
        // childGroupId (product/sum parts)
        // childId (number of cell in product part)
        //
        //
        //
        /*

         ∀ (cell, nextCell) in line
         nextCell.row.pos == cell.row.pos ||
         nextCell.row.pos == cell.row.pos + 1
         for first cell in line row.pos = 0

         ∀ (cell, nextCell) in row
         nextCell.group.pos = cell.group.pos ||
         nextCell.group.pos == cell.group.pos + 1
         for first cell in row group.pos = 0

         ∀ (cell, nextCell) in group
         nextCell.childGroup.pos = cell.childGroup.pos ||
         nextCell.childGroup.pos = cell.childGroup.pos + 1
         for first cell in row childGroup.pos = 0

         ∀ (cell, nextCell) in childGroup
         nextCell.child.pos = cell.child.pos ||
         nextCell.child.pos = cell.child.pos + 1

         */
        /*
            row1, row2, row

            row1,             row2,             row3,
            grp1, grp2, grp3, grp1, grp2, grp3, grp1, grp2



         */


        /**
         * line.pos
         * line.row
         * line.row.pos
         * line.row.group
         * line.row.group.pos
         * line.row.group.child
         */

        /**
         * row.first = row.pos = 0
         * row.last = row.pos == row.lastIndex
         *
         */
        // row[0].group
    }
}