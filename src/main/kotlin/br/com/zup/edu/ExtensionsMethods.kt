package br.com.zup.edu

import br.com.zup.edu.chavepix.*
import br.com.zup.edu.clients.BancoCentralBrasilClient
import com.google.protobuf.Timestamp
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.time.ZoneId

fun PixKeyRequest?.paraChavePixRequest(): ChavePixRequest {
    return ChavePixRequest(
            chave = this!!.chave,
            idPortador = this.idPortador,
            conta = TipoDaConta.valueOf(this.conta.name),
            tipoChave = TipoDaChave.valueOf(this.tipo.name)
    )

}
fun RemoveKeyRequest?.paraRemoverChaveRequest():RemoverChaveRequest {
   return RemoverChaveRequest(this!!.pixId,this.idPortador)
}
fun Chave.paraCreatePixRequest(): BancoCentralBrasilClient.CriarChaveRequest {
    val bankAccount = BancoCentralBrasilClient.BankAccount(
            this.contaAssociada.ispbInstituicao,
            this.contaAssociada.agencia,
            this.contaAssociada.numero,
            when(this.conta){
                TipoDaConta.CONTA_POUPANCA -> BancoCentralBrasilClient.AccountType.SVGS
                TipoDaConta.CONTA_CORRENTE ->BancoCentralBrasilClient.AccountType.CACC
                else -> throw IllegalStateException("TIPO DE CONTA INVALIDA")
            })
    val ownerRequest = BancoCentralBrasilClient.OwnerRequest(
            BancoCentralBrasilClient.TypeOnner.NATURAL_PERSON,
            this.contaAssociada.nome,
            this.contaAssociada.cpf)
    return BancoCentralBrasilClient.CriarChaveRequest(
            key = this.chave,
            keyType = when(this.tipoChave){
                TipoDaChave.CPF -> BancoCentralBrasilClient.KeyType.CPF
                TipoDaChave.TELEFONECELULAR -> BancoCentralBrasilClient.KeyType.PHONE
                TipoDaChave.EMAIL -> BancoCentralBrasilClient.KeyType.EMAIL
                TipoDaChave.CHAVEALEATORIA -> BancoCentralBrasilClient.KeyType.RANDOM
                TipoDaChave.SEM_TIPOCHAVE -> throw IllegalStateException("TIPO DE CONTA INVALIDA")
            } ,
            bankAccount = bankAccount,
            owner = ownerRequest
    )

}
fun Chave.paraRemovePixKeyRequest(): BancoCentralBrasilClient.DeletePixKeyRequest{
    return BancoCentralBrasilClient.DeletePixKeyRequest(this.chave,this.contaAssociada.ispbInstituicao)
}
 fun Timestamp.Builder.getTime(): Timestamp {
    val toInstant = LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()
    return Timestamp.newBuilder()
            .setSeconds(toInstant.epochSecond)
            .setNanos(toInstant.nano).build()
}
fun LocalDateTime.toTimeStampGrpc(): Timestamp {
    val toInstant = this.atZone(ZoneId.of("UTC")).toInstant()
    return Timestamp.newBuilder()
            .setSeconds(toInstant.epochSecond)
            .setNanos(toInstant.nano).build()
}
fun Chave.paraSearchKeyReponse(): SearchKeyResponse {
    val contaBancaria = ContaBancaria.newBuilder()
            .setNomeInstituicao(this.contaAssociada.nomeInstituicao)
            .setNumeroConta(this.contaAssociada.numero)
            .setTipoConta(TipoConta.valueOf(this.conta.name))
            .build()
    return SearchKeyResponse.newBuilder()
            .setChave(this.chave)
            .setIdPix(this.id)
            .setIdPortador(this.idPortador)
            .setCpfPortador(this.contaAssociada.cpf)
            .setTipoChave(TipoChave.valueOf(this.tipoChave.name))
            .setNomePortador(this.contaAssociada.nome)
            .setCriadoEm(this.criadoEm.toTimeStampGrpc())
            .setConta(contaBancaria)
            .build()
}
fun Chave.paraSearchKeyExternalResponse(): SearchKeyExternalResponse {
    val contaBancaria = ContaBancaria.newBuilder()
            .setNomeInstituicao(this.contaAssociada.nomeInstituicao)
            .setNumeroConta(this.contaAssociada.numero)
            .setTipoConta(TipoConta.valueOf(this.conta.name))
            .build()
    return SearchKeyExternalResponse.newBuilder()
            .setChave(this.chave)
            .setCpfPortador(this.contaAssociada.cpf)
            .setTipoChave(TipoChave.valueOf(this.tipoChave.name))
            .setNomePortador(this.contaAssociada.nome)
            .setCriadoEm(this.criadoEm.toTimeStampGrpc())
            .setConta(contaBancaria)
            .build()
}
fun BancoCentralBrasilClient.CriarChaveResponse.paraSearchKeyExternalResponse(): SearchKeyExternalResponse {

    val contaBancaria = ContaBancaria.newBuilder()
            .setNomeInstituicao("ITAÚ UNIBANCO S.A.")
            .setNumeroConta(this.bankAccount.accountNumber)
            .setTipoConta(
                    when (this.bankAccount.accountType) {
                        BancoCentralBrasilClient.AccountType.SVGS -> TipoConta.CONTA_POUPANCA
                        BancoCentralBrasilClient.AccountType.CACC -> TipoConta.CONTA_CORRENTE
                        else -> throw IllegalStateException("TIPO DE CONTA INVALIDA")
                    })
            .build()
    return SearchKeyExternalResponse.newBuilder()
            .setChave(this.key)
            .setCpfPortador(this.owner.taxIdNumber)
            .setTipoChave(when (this.keyType) {
                BancoCentralBrasilClient.KeyType.CPF -> TipoChave.CPF
                BancoCentralBrasilClient.KeyType.PHONE -> TipoChave.TELEFONECELULAR
                BancoCentralBrasilClient.KeyType.EMAIL -> TipoChave.EMAIL
                BancoCentralBrasilClient.KeyType.RANDOM -> TipoChave.CHAVEALEATORIA
                BancoCentralBrasilClient.KeyType.CNPJ -> throw IllegalStateException("TIPO DE CHAVE INVALIDA")
            })
            .setNomePortador(this.owner.name)
            .setCriadoEm(this.createdAt.toTimeStampGrpc())
            .setConta(contaBancaria)
            .build()
}