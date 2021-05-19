package br.com.zup.edu.chavepix

import br.com.zup.edu.clients.BancoCentralBrasilClient
import br.com.zup.edu.exceptions.ChavePixNotFoundException
import br.com.zup.edu.exceptions.ClientNotFoundException
import br.com.zup.edu.exceptions.ConstraintValidationsViolityException
import br.com.zup.edu.exceptions.NaoPortadorDaChaveException
import br.com.zup.edu.paraRemovePixKeyRequest
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagerRemoveKeyService(
        @Inject private val repository: ChaveRepository,
        @Inject private val clientBcb:BancoCentralBrasilClient
) {
    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    fun removerChave(removerChaveRequest: RemoverChaveRequest):Boolean {
        if (removerChaveRequest.idPortador.isBlank() || removerChaveRequest.idChave.isBlank()) {
            throw ConstraintValidationsViolityException("IdPix e/ou IdPortador devem ser preenchidos.")
        }
        val possivelChave = repository.findById(removerChaveRequest.idChave)
        if (possivelChave.isEmpty) {
            throw ChavePixNotFoundException("Esta chave inexistente.")
        }
        val chave = possivelChave.get()
        if (!chave.idPortador.equals(removerChaveRequest.idPortador)) {
            throw NaoPortadorDaChaveException("Somente o portador da chave tem premissao para remove-la.")
        }
        val responseBcb = clientBcb.deletarChave(chave.chave, chave.paraRemovePixKeyRequest())
        if (responseBcb.status().code!=200){
            throw ClientNotFoundException("Falha ao remover chave pix ao banco central.")
        }
        repository.delete(chave)
        return true
    }
}