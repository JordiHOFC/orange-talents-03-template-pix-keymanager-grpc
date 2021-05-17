package br.com.zup.edu.chavepix

import br.com.zup.edu.PixKeyRequest
import br.com.zup.edu.TipoChave
import java.util.*
import javax.validation.Valid

public fun PixKeyRequest.toChave(): Chave {
    if (this.tipo.equals(TipoChave.CHAVEALEATORIA)){
        return Chave(
            UUID.randomUUID().toString(),
            this.idPortador,
            TipoDaConta.valueOf(this.conta.name),
            TipoDaChave.valueOf(this.tipo.name)
        )
    }
    val funcao= fun (@Valid request:ChavePixRequest): Chave= request.paraChave()
    var chavePixRequest = ChavePixRequest(
        this.chave,
        this.idPortador,
        TipoDaConta.valueOf(this.conta.name),
        TipoDaChave.valueOf(this.tipo.name)
    )

    return funcao(chavePixRequest)
}