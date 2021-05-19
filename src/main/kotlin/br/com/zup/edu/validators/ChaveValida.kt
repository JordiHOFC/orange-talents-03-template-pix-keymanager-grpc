package br.com.zup.edu.validators

import br.com.zup.edu.chavepix.ChavePixRequest
import br.com.zup.edu.chavepix.TipoDaChave
import br.com.zup.edu.exceptions.ConstraintValidationsViolityException


class ChaveValidaValidator(){
     fun isValid(request:ChavePixRequest): Boolean {
        if (request.tipoChave.equals(TipoDaChave.CHAVEALEATORIA) && request.chave.isNullOrBlank()) {
            return true
        }
        else if (!request.tipoChave.equals(TipoDaChave.CHAVEALEATORIA) && request.chave.length<=77) {
            if (validaValorParachave(chave = request.chave)) {
                return true
            }
        }
        throw ConstraintValidationsViolityException("Chave em formato Invalido.")
    }

}
 fun validaValorParachave(chave: String): Boolean {
    when {
        chave.matches("\\+[1-9][0-9]\\d{1,14}".toRegex()) -> {
            return true
        }
        chave.matches("[0-9]{11}".toRegex()) -> {
            return true
        }
        chave.matches("^[A-Za-z0-9+_.-]+@(.+)\$".toRegex()) -> {
            return true
        }
        else -> return false
    }
}