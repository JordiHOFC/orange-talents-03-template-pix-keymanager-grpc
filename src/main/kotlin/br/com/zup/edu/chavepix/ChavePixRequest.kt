package br.com.zup.edu.chavepix

import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class ChavePixRequest(
        @field:Size(max = 77) @field:NotBlank val chave: String,
        @field:NotBlank val idPortador: String,
        @field:NotBlank val conta: TipoDaConta,
        @field:NotBlank val tipoChave: TipoDaChave

) {
    fun paraChave(contaAssociada: ContaAssociada):Chave{
        return Chave(
                chave=if(tipoChave.equals(TipoDaChave.CHAVEALEATORIA)) { UUID.randomUUID().toString()}else {chave},
                idPortador=idPortador,
                conta=conta,
                tipoChave=tipoChave,
                contaAssociada=contaAssociada
        )
    }
}
