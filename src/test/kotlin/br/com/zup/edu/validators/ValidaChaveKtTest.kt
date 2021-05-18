package br.com.zup.edu.validators

import br.com.zup.edu.chavepix.validaValorParachave
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import junit.framework.Assert.assertTrue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


@MicronautTest(transactional = false)
internal class ValidaChaveKtTest {

    @Test
    fun naoDeveSerValido(){
        val listaInvalido = arrayListOf("@j", "4451231321", "12312344", "a.c", "123134121", "123456723")
        listaInvalido.forEach {
            val valid = validaValorParachave(it)
            assertFalse(valid)
        }
    }
    @Test
    fun deveSerValido(){
        val validos = arrayListOf("jordi@jorde.com.br", "jordi.silva@zup.com.br", "+553499927654312", "+553498837654312", "06283318002", "56096645003")
        validos.forEach {
            val valid = validaValorParachave(it)
            assertTrue(valid)
        }
    }


}