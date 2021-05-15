package br.com.zup.edu.chavepix

import br.com.zup.edu.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

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
        if (!request?.tipo?.equals(TipoChave.CHAVEALEATORIA)!!) {
            //1
            if (request.chave.isBlank() || request.chave.isEmpty()) {
                val e = Status.INVALID_ARGUMENT
                var asRuntimeException = e.withDescription(
                    "Deve conter um valor para chave, caso o tipo seja diferente de Aleatoria."
                ).asRuntimeException()
                responseObserver!!.onError(
                    asRuntimeException

                )
                return
            }
            val valida = validaValorParachave(request.chave)
            //1
            if (valida) {
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

        val chave = request.toChaveAleatoria()
        repository.save(chave)
        responseObserver!!.onNext(PixKeyResponse.newBuilder().setIdPix(chave.id).build())
        responseObserver.onCompleted()

    }
}

private fun PixKeyRequest.toChave():Chave {
    return Chave(
        this.chave,
        this.idPortador,
        TipoDaConta.valueOf(this.conta.name),
        TipoDaChave.valueOf(this.tipo.name)
    )
}

private fun PixKeyRequest.toChaveAleatoria(): Chave{
    return Chave(
        UUID.randomUUID().toString(),
        this.idPortador,
        TipoDaConta.valueOf(this.conta.name),
        TipoDaChave.valueOf(this.tipo.name)
    )
}



