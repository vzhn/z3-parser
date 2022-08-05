package me.vzhilin.gr.derivation

import me.vzhilin.gr.rules.parseDerivation
import me.vzhilin.gr.simpleGrammar
import kotlin.test.Test
import kotlin.test.assertEquals

class DerivationValidationTests {
    private val input =
    """ 'λx.xλy.yy'                              #  V(3)      
        'λx.' V('x') 'λy.yy'                     #  T(3)      
        'λx.' T('x') 'λy.yy'                     #  V(1)      
        'λ' V('x') '.' T('x') 'λy.yy'            #  ABST(0:3) 
        ABST('λx.x') 'λy.yy'                     #  V(2)      
        ABST('λx.x') 'λ' V('y') '.yy'            #  V(4)      
        ABST('λx.x') 'λ' V('y') '.' V('y') 'y'   #  T(4)      
        ABST('λx.x') 'λ' V('y') '.' T('y') 'y'   #  ABST(1:4) 
        ABST('λx.x') ABST('λy.y') 'y'            #  T(0)      
        T('λx.x') ABST('λy.y') 'y'               #  T(1)      
        T('λx.x') T('λy.y') 'y'                  #  APP(0:1)  
        APP('λx.xλy.y') 'y'                      #  T(0)      
        T('λx.xλy.y') 'y'                        #  V(1)      
        T('λx.xλy.y') V('y')                     #  T(1)      
        T('λx.xλy.y') T('y')                     #  APP(0:1)   
        APP('λx.xλy.yy')                         #  T(0)      
        T('λx.xλy.yy')
    """.trimIndent()

    @Test
    fun test() {
        val g = simpleGrammar()
        val derivation = g.parseDerivation(input)

        assertEquals(Ok, DerivationValidator().validate(derivation))
    }
}