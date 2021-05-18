package br.com.zup.edu

import br.com.zup.edu.chavepix.ChavePixRequest
import br.com.zup.edu.chavepix.TipoDaChave
import br.com.zup.edu.chavepix.TipoDaConta

fun PixKeyRequest?.paraChavePixRequest(): ChavePixRequest {
    return ChavePixRequest(
            chave = this!!.chave,
            idPortador = this.idPortador,
            conta = TipoDaConta.valueOf(this.conta.name),
            tipoChave = TipoDaChave.valueOf(this.tipo.name)
    )

}