package br.com.zup.edu.chavepix

import br.com.zup.edu.clients.ItauLegacyClient
import br.com.zup.edu.exceptions.ChavePixExistenteException
import br.com.zup.edu.exceptions.ClientNotFoundException
import br.com.zup.edu.validators.ChaveValidaValidator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton

class ManagerRegisterKeyService(
        @Inject val chaveRepository: ChaveRepository,
        @Inject val clientItau: ItauLegacyClient
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
        chaveRepository.save(chave)
        return chave
    }


}
