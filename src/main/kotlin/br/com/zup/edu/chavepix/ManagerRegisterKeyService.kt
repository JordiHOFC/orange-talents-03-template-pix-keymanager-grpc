package br.com.zup.edu.chavepix

import br.com.zup.edu.clients.BancoCentralBrasilClient
import br.com.zup.edu.clients.ItauLegacyClient
import br.com.zup.edu.exceptions.ChavePixExistenteException
import br.com.zup.edu.exceptions.ClientNotFoundException
import br.com.zup.edu.paraCreatePixRequest
import br.com.zup.edu.validators.ChaveValidaValidator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton

class ManagerRegisterKeyService(
        @Inject val chaveRepository: ChaveRepository,
        @Inject val clientItau: ItauLegacyClient,
        @Inject val clientBcb: BancoCentralBrasilClient
) {
    fun registrarChave(chavePixRequest: ChavePixRequest): Chave {

        if (!chavePixRequest.tipoChave.equals(TipoDaChave.CHAVEALEATORIA)) {
            if (chaveRepository.existsByChave(chavePixRequest.chave)) {
                throw ChavePixExistenteException("Chave já cadastrada.")
            }
        }
        ChaveValidaValidator().isValid(chavePixRequest)

        val responseItau = clientItau.findClient(chavePixRequest.idPortador, chavePixRequest.conta.name)

        if (responseItau.status.code != 200) {
            throw ClientNotFoundException("Nao exite cadastro para este cliente.")
        }
        val chave = chavePixRequest.paraChave(responseItau.body()!!.paraContaAssociada())
        val responseBcb = clientBcb.cadastrarChave(chave.paraCreatePixRequest())
        if (responseBcb.status.code!=201){
            throw ClientNotFoundException("Falha ao sincronizar ao Banco Central do Brasil.")
        }
        chave.atualizaChave(responseBcb.body()!!.key)
        chave.atualizaDataCriacao(responseBcb.body()!!.createdAt)

        chaveRepository.save(chave)
        return chave
    }


}
