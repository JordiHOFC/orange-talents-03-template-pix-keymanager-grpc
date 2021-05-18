package br.com.zup.edu.chavepix

import br.com.zup.edu.PixKeyManagerGRpcServiceGrpc
import br.com.zup.edu.PixKeyRequest
import br.com.zup.edu.PixKeyResponse
import br.com.zup.edu.exceptions.ExceptionGRPC
import br.com.zup.edu.handler.ErrorHandler
import br.com.zup.edu.paraChavePixRequest
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class CadastrarNovaChavePixServer(
        @Inject private val service: ManagerKeyServer
) : PixKeyManagerGRpcServiceGrpc.PixKeyManagerGRpcServiceImplBase() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun cadastrarChave(request: PixKeyRequest?, responseObserver: StreamObserver<PixKeyResponse>?) {
//        try {
            val chave = service.registrarChave(request.paraChavePixRequest())
            responseObserver!!.onNext(PixKeyResponse.newBuilder()
                    .setIdPix(chave.id)
                    .build()
            )
            responseObserver.onCompleted()


//        }catch (e: ExceptionGRPC){
//            e.printStackTrace()
//            responseObserver!!.onError(e.getException().asRuntimeException())
//
//        }
    }
}


