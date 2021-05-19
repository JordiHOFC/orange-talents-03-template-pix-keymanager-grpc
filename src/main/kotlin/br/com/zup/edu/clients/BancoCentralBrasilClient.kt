package br.com.zup.edu.clients

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client(value = "\${bcb.accounts.url}")
interface BancoCentralBrasilClient {
    @Post("/api/v1/pix/keys")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun cadastrarChave(@Body request: CriarChaveRequest): HttpResponse<CriarChaveResponse>

    @Delete("/api/v1/pix/keys/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun deletarChave(@PathVariable key: String,@Body request: DeletePixKeyRequest):HttpResponse<DeletePixKeyResponse>


    data class CriarChaveRequest(val key: String, val keyType: KeyType, val bankAccount: BankAccount, val owner: OwnerRequest)
    data class CriarChaveResponse(val key: String, val keyType: KeyType, val bankAccount: BankAccount, val owner: OwnerRequest, val createdAt: LocalDateTime)
    enum class KeyType { CPF, CNPJ, PHONE, EMAIL, RANDOM }
    enum class AccountType { CCAC, SVGS }
    data class BankAccount(val participant: String, val branch: String, val accountNumber: String, val accountType: AccountType)
    enum class TypeOnner { NATURAL_PERSON, LEGAL_PERSON }
    data class OwnerRequest(val type: TypeOnner, val name: String, val taxIdNumber: String)
    data class DeletePixKeyRequest(val key: String, val participant: String)
    data class DeletePixKeyResponse(val key: String, val participant: String, val deletedAt: LocalDateTime)
}