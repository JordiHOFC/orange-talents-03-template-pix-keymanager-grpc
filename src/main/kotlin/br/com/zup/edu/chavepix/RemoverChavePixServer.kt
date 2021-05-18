package br.com.zup.edu.chavepix

import br.com.zup.edu.RemoveKeyManagerGrpcServiceGrpc
import br.com.zup.edu.RemoveKeyRequest
import br.com.zup.edu.RemoveKeyResponse
import br.com.zup.edu.handler.ErrorHandler
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RemoverChavePixServer(
        val repository: ChaveRepository
):RemoveKeyManagerGrpcServiceGrpc.RemoveKeyManagerGrpcServiceImplBase() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun removerChave(request: RemoveKeyRequest?, responseObserver: StreamObserver<RemoveKeyResponse>?) {
        logger.info(request.toString())
        val nullexception= Status.INVALID_ARGUMENT.withDescription("IdPix e/ou IdPortador devem ser preenchidos.").asRuntimeException()

        if (request!=null) {
            if (request.idPortador.isNullOrBlank() || request.pixId.isNullOrBlank()) {
                responseObserver!!.onError(nullexception)
                return
            }

            val possivelChave = repository.findById(request.pixId)
            if (possivelChave.isEmpty) {
                val e = Status.NOT_FOUND.withDescription("Esta chave inexistente.").asRuntimeException()
                responseObserver!!.onError(e)
                return
            }
            val chave = possivelChave.get()
            if (!chave.idPortador.equals(request.idPortador)) {
                val e = Status.INVALID_ARGUMENT.withDescription("Somente o portador da chave tem premissao para remove-la.").asRuntimeException()
                responseObserver!!.onError(e)
                return
            }
            repository.delete(chave)
            responseObserver!!.onNext(RemoveKeyResponse.getDefaultInstance())
            responseObserver.onCompleted()

        }
    }
}