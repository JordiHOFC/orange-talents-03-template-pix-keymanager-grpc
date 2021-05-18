package br.com.zup.edu.clients

import br.com.zup.edu.chavepix.ContaAssociada
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "\${itau.accounts.url}")
interface ItauLegacyClient {
    @Get("/api/v1/clientes/{clienteId}/contas")
    fun findClient(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<ResponseItau>

    data class ResponseItau(val tipo: String,
                            val agencia: String,
                            val numero: String,
                            val titular: Titular,
                            val instituicao: Instituicao

    ) {
        fun paraContaAssociada(): ContaAssociada {
            return ContaAssociada(
                    nome = titular.nome,
                    agencia = agencia,
                    cpf = titular.cpf,
                    numero = numero,
                    nomeInstituicao = instituicao.nome,
                    ispbInstituicao = instituicao.ispb
            )
        }
    }

    data class Titular(val nome: String, val id: String, val cpf: String)
    data class Instituicao(val nome: String, val ispb: String)
}


