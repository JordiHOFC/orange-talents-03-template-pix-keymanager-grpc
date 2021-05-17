package br.com.zup.edu.chavepix

import br.com.zup.edu.*
import br.com.zup.edu.chavepix.validator.ValidUUID
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
//5
class CadastrarChavePixServer(
    //1
    val repository: ChaveRepository
) : PixKeyManagerGRpcServiceGrpc.PixKeyManagerGRpcServiceImplBase() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun cadastrarChave(request: PixKeyRequest?, responseObserver: StreamObserver<PixKeyResponse>?) {
        logger.info(request.toString())
        //1
        var requestValid = validateType(request)


       
            val existsByChave = repository.existsByChave(request.chave)
            //1
            if (existsByChave) {
                val e = Status.ALREADY_EXISTS
                responseObserver!!.onError(
                    e.withDescription(
                        "Chave já cadastrada."
                    ).asRuntimeException()
                )
                return
            }
            val chave = request.toChave()
            repository.save(chave)

            responseObserver!!.onNext(PixKeyResponse.newBuilder().setIdPix(chave.id).build())
            responseObserver.onCompleted()
            return
        }
        val e = Status.INVALID_ARGUMENT
        responseObserver!!.onError(
            e.withDescription(
                "O valor informado não corresponde ao ${request.tipo.name}."
            ).asRuntimeException()
        )
        return;


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
}







