package br.com.zup.edu.chavepix

import br.com.zup.edu.SearchKeyExternalRequest
import br.com.zup.edu.SearchKeyExternalResponse
import br.com.zup.edu.SearchKeyRequest
import br.com.zup.edu.clients.BancoCentralBrasilClient
import br.com.zup.edu.exceptions.ChavePixNotFoundException
import br.com.zup.edu.exceptions.ConectionBancoCentralNotFoundException
import br.com.zup.edu.exceptions.ConstraintValidationsViolityException
import br.com.zup.edu.paraSearchKeyExternalResponse
import br.com.zup.edu.validators.validaValorParachave
import io.micronaut.http.HttpStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagerSearchKeyService(
        @Inject private val repository: ChaveRepository,
        @Inject private val clientBcb: BancoCentralBrasilClient
) {
    fun buscarChaveNoKeyManager(request: SearchKeyRequest?): Chave {
        if (request!!.pixId.isNullOrBlank() || request.idPortador.isNullOrBlank()){
            throw ConstraintValidationsViolityException("PixId e/ou IdPortador devem ser preenchidos.")
        }
        val possivelChave = repository.findById(request.pixId)
        if(possivelChave.isEmpty){
            throw ChavePixNotFoundException("Chave Pix não existente.")
        }
        return possivelChave.get()
    }

    fun buscarChaveParaSistemasExternos(request: SearchKeyExternalRequest?): SearchKeyExternalResponse {
        if (request!!.chave.isNullOrBlank()) {
            throw ConstraintValidationsViolityException("O campo chave deve ser preenchido.")
        }
        if (!validaValorParachave(request.chave)) {
            throw ConstraintValidationsViolityException("Chave em formato invalido")
        }
        val possivelChave = repository.findByChave(request.chave)
        if (possivelChave.isEmpty) {
            val responseBcb = clientBcb.buscarChave(request.chave)
            when (responseBcb.status) {
                HttpStatus.NOT_FOUND -> throw ChavePixNotFoundException("Chave Pix não existente.")
                HttpStatus.OK -> return responseBcb.body()!!.paraSearchKeyExternalResponse()
            }

        }else{
            return possivelChave.get().paraSearchKeyExternalResponse()
        }
        throw ConectionBancoCentralNotFoundException("Falha ao consultar ao banco central.")
    }


}