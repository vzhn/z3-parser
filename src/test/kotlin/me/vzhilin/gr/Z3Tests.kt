package me.vzhilin.gr

import com.microsoft.z3.Context
import kotlin.test.Test

class Z3Tests {
    @Test
    fun test() {
        val ctx = Context()
        val solver = ctx.mkSolver("LIA")
        solver.check()
    }
}