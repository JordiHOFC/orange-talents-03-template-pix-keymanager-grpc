package br.com.zup.edu.chavepix

import br.com.zup.edu.PixKeyRequest
import br.com.zup.edu.chavepix.validator.ValidUUID
import javax.persistence.Column
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class ChavePixRequest(
    @field:ChaveValida @field:Size(max = 77) @field:NotBlank val chave: String,
    @field:ValidUUID @field:NotBlank val idPortador: String,
    @field:NotBlank    val tipoConta: TipoDaConta,
    @field:NotBlank val tipoChave: TipoDaChave
    )
{


    fun paraChave(): Chave {
        return Chave(
            this.chave,
            this.idPortador,
            this.tipoConta,
            this.tipoChave
        )
    }
}