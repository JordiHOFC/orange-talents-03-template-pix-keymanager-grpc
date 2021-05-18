package br.com.zup.edu.validators

import br.com.zup.edu.chavepix.ChavePixRequest
import br.com.zup.edu.chavepix.TipoDaChave
import br.com.zup.edu.chavepix.validaValorParachave
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
        throw ConstraintValidationsViolityException("Formato chave em formato Invalido")
    }

}