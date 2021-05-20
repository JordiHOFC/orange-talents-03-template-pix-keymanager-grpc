package br.com.zup.edu.chavepix

import br.com.zup.edu.*
import br.com.zup.edu.clients.BancoCentralBrasilClient
import br.com.zup.edu.exceptions.ChavePixNotFoundException
import br.com.zup.edu.exceptions.ConectionBancoCentralNotFoundException
import br.com.zup.edu.exceptions.ConstraintValidationsViolityException
import br.com.zup.edu.handler.ErrorHandler
import br.com.zup.edu.validators.validaValorParachave
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class BuscarChavePixServer(
        @Inject private val service: ManagerSearchKeyService
) : SearchPixKeyManagerGrpcServiceGrpc.SearchPixKeyManagerGrpcServiceImplBase(), ServerGRPC {
    private val LOGGER = LoggerFactory.getLogger(this::class.java)
    override fun consultarChaveKeyManager(request: SearchKeyRequest?, responseObserver: StreamObserver<SearchKeyResponse>?) {
        LOGGER.info("\n" + request.toString())
        val chave = service.buscarChaveNoKeyManager(request)
        responseObserver!!.onNext(chave.paraSearchKeyReponse())
        responseObserver.onCompleted()

    }

    override fun consultaChaveExternal(request: SearchKeyExternalRequest?, responseObserver: StreamObserver<SearchKeyExternalResponse>?) {
        LOGGER.info("\n" + request.toString())
        val response = service.buscarChaveParaSistemasExternos(request)
        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }

}

