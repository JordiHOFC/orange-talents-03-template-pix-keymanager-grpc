package br.com.zup.edu

import br.com.zup.edu.chavepix.*
import br.com.zup.edu.clients.BancoCentralBrasilClient
import java.lang.IllegalStateException

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
                TipoDaConta.CONTA_CORRENTE ->BancoCentralBrasilClient.AccountType.CCAC
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