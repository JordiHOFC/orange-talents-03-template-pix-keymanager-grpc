package br.com.zup.edu.chavepix

import br.com.zup.edu.*
import br.com.zup.edu.chavepix.validator.ValidUUID
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.executable.ValidateOnExecution

@Singleton
@Validated
@br.com.Open
//5
class CadastrarChavePixServer(
        //1
        val repository: ChaveRepository
) : PixKeyManagerGRpcServiceGrpc.PixKeyManagerGRpcServiceImplBase() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    @ValidateOnExecution
    override fun cadastrarChave(request: PixKeyRequest?, responseObserver: StreamObserver<PixKeyResponse>?) {
        logger.info(request.toString())
        //1
        var requestValid = validateType(request)
        if(requestValid!=null){
            responseObserver!!.onError(requestValid)
            return
        }

        val existsByChave = repository.existsByChave(request!!.chave)
        //1
        if (existsByChave) {
            responseObserver!!.onError(
                    createException(Status.ALREADY_EXISTS,"Chave já cadastrada.")
            )
            return
        }
        try {
            val chave=request!!.toChave()
            repository.save(chave)
            responseObserver!!.onNext(PixKeyResponse.newBuilder().setIdPix(chave.id).build())
            responseObserver.onCompleted()
        } catch (e: ConstraintViolationException) {
            responseObserver!!.onError(
                   createException(Status.INVALID_ARGUMENT,"O valor informado não corresponde ao ${request.tipo.name}.")
            )
            return
        }
    }
}

private fun validateType(
        request: PixKeyRequest?,
): StatusRuntimeException? {
    if (!request?.tipo?.equals(TipoChave.CHAVEALEATORIA)!! && (request.chave.isBlank() || request.chave.isEmpty())) {
        val msg = "Deve conter um valor para chave, caso o tipo seja diferente de Aleatoria."
        val e = createException(Status.INVALID_ARGUMENT, msg)
        return e

    } else if (request.tipo.equals(TipoChave.CHAVEALEATORIA) && request.chave.isNotBlank()) {
        val msg = "Nao deve conter um valor para chave caso o tipo seja de Aleatoria."
        val e = createException(Status.INVALID_ARGUMENT, msg)
        return e

    }
    return null

}

private fun createException(status: Status, message: String): StatusRuntimeException {
    return status.withDescription(
            message
    ).asRuntimeException()
}








