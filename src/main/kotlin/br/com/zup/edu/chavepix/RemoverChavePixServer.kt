package br.com.zup.edu.chavepix

import br.com.zup.edu.RemoveKeyManagerGrpcServiceGrpc
import br.com.zup.edu.RemoveKeyRequest
import br.com.zup.edu.RemoveKeyResponse
import br.com.zup.edu.handler.ErrorHandler
import br.com.zup.edu.paraRemoverChaveRequest
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RemoverChavePixServer(
        @Inject private val removeKeyService: ManagerRemoveKeyService
) : RemoveKeyManagerGrpcServiceGrpc.RemoveKeyManagerGrpcServiceImplBase(), ServerGRPC {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun removerChave(request: RemoveKeyRequest?, responseObserver: StreamObserver<RemoveKeyResponse>?) {
        logger.info(request.toString())

        var removerChaveRequest = request.paraRemoverChaveRequest()

        removeKeyService.removerChave(removerChaveRequest)

        responseObserver!!.onNext(RemoveKeyResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }

}
