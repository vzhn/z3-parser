package me.vzhilin.gr.report

import me.vzhilin.gr.Cell
import me.vzhilin.gr.rules.Grammar
import java.io.File
import java.io.PrintWriter

fun writeSvg(file: File, input: String, g: Grammar, data: Map<Int, Map<Pair<Int, Int>, Map<Int, List<Cell>>>>) {
    val container = buildContainer(input, g, data)
    val commands = draw(container)

    val pw = PrintWriter(file)
    pw.write("<svg xmlns='http://www.w3.org/2000/svg' width='${container.width}' height='${container.height}' version='1.1'><g>")
    commands.map { rect ->
        pw.write("<rect width='${rect.width}' height='${rect.height}' stroke='black' stroke-width='2' fill='${rect.fill}' x='${rect.x}' y='${rect.y}'/>\n")
        if (rect.centerText.isNotEmpty()) {
            val x = rect.x + 35
            val y = rect.y + 64
            pw.write("<text x='$x' y='$y' font-size='48px'>${rect.centerText}</text>")
        }

        if (rect.upperText.isNotEmpty()) {
            val x = rect.x + 16
            val y = rect.y + 16
            pw.write("<text x='$x' y='$y' font-size='16px'>${rect.upperText}</text>")
        }
    }
    pw.write("</g></svg>")
    pw.close()
}

fun buildContainer(
    input: String,
    g: Grammar,
    data: Map<Int, Map<Pair<Int, Int>, Map<Int, List<Cell>>>>
): Container {
    return Container(ContainerType.COL, "", "", data.map { (rowId, rowData) ->
        Container(ContainerType.ROW, "row #$rowId", "", rowData.map { (groupIdRuleId, groupData) ->
            val (groupId, ruleId) = groupIdRuleId
            val rule = g.rule(ruleId)
            Container(ContainerType.ROW, "rule #$ruleId: ${rule.name}, gr #$groupId", "", groupData.map { (subGroupId, cells) ->
                Container(ContainerType.ROW, "sgroup: ${subGroupId}", "", cells.map { cell ->
                    Container(ContainerType.CELL, "#${cell.id}", input[cell.col].toString(), emptyList())
                })
            })
        }).also {
            it.fill = "#D5E8D4"
        }
    })
}

data class SVGRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val fill: String,
    val upperText: String,
    val centerText: String
)

fun draw(c: Container): List<SVGRect> {
    val result = mutableListOf<SVGRect>()

    fun render(x: Int, y: Int, c: Container) {
        val width = c.width
        val height = c.height
        result.add(SVGRect(x, y, width, height, c.fill, c.upperText, c.centerText))

        var chx = x + c.padding
        var chy = y + c.padding
        c.children.forEachIndexed { index, ch ->
            render(chx, chy, ch)

            when (c.type) {
                ContainerType.ROW -> chx += c.spacing + ch.width
                ContainerType.COL -> chy += c.spacing + ch.height
                ContainerType.CELL -> Unit
            }
        }
    }

    render(0, 0, c)
    return result
}

val cellWidth = 64
val cellHeight = 64

enum class ContainerType { ROW, COL, CELL }
data class Container(
    val type: ContainerType,
    val upperText: String,
    val centerText: String,
    val children: List<Container>
) {
    var fill: String = "white"
    val padding = 25
    val spacing = 25

    private val childSpacing by lazy { Math.max(0, children.size - 1) * spacing }

    val width: Int by lazy {
        padding * 2 + when (type) {
            ContainerType.ROW -> childSpacing + children.sumOf(Container::width)
            ContainerType.COL -> children.maxOf(Container::width)
            ContainerType.CELL -> cellWidth
        }
    }

    val height: Int by lazy {
        padding * 2 + when (type) {
            ContainerType.ROW -> children.maxOf(Container::height)
            ContainerType.COL -> childSpacing + children.sumOf(Container::height)
            ContainerType.CELL -> cellHeight
        }
    }
}