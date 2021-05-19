package br.com.zup.edu.chavepix

import br.com.zup.edu.RemoveKeyManagerGrpcServiceGrpc
import br.com.zup.edu.RemoveKeyRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID.randomUUID
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoverChavePixServerTest(
    @Inject    private val grpcClient: RemoveKeyManagerGrpcServiceGrpc.RemoveKeyManagerGrpcServiceBlockingStub,
    @Inject    private val repository: ChaveRepository
    ) {
    @Test
    fun `nao deve remover uma chave que nao existe`() {
        val request = RemoveKeyRequest.newBuilder()
                .setIdPortador(randomUUID().toString())
                .setPixId(randomUUID().toString())
                .build()
        var e = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }
        assertEquals(Status.NOT_FOUND,e.status.code.toStatus())
        assertEquals(e.status.description,"Esta chave inexistente.")

    }

    @Test
    fun `nao deve remover caso a request seja nulla`() {
        val requestPixIdNull = RemoveKeyRequest.newBuilder()
                .setIdPortador(randomUUID().toString())
                .build()
        val requestIdPortadorNull = RemoveKeyRequest.newBuilder()
                .setPixId(randomUUID().toString())
                .build()
        val requestAllNull = RemoveKeyRequest.getDefaultInstance()
        val list = arrayListOf(requestAllNull, requestIdPortadorNull, requestPixIdNull)
        list.forEach { r->
            val request = assertThrows<StatusRuntimeException> {
                grpcClient.removerChave(r)
            }
            assertEquals(Status.INVALID_ARGUMENT,request.status.code.toStatus())
            assertEquals(request.status.description,"IdPix e/ou IdPortador devem ser preenchidos.")
        }
    }
//    @Test
//    fun `apenas o dono da chave pode remove-la`(){
//
//        val chave = Chave("37815616070",
//                randomUUID().toString(),
//                TipoDaConta.CONTA_POUPANCA,
//                TipoDaChave.CPF)
//        repository.save(chave)
//        val request = RemoveKeyRequest.newBuilder()
//                .setPixId(chave.id)
//                .setIdPortador(randomUUID().toString())
//                .build()
//
//        val e = assertThrows<StatusRuntimeException> {
//            grpcClient.removerChave(request)
//        }
//        assertEquals(Status.INVALID_ARGUMENT,e.status.code.toStatus())
//        assertEquals(e.status.description,"Somente o portador da chave tem premissao para remove-la.")
//
//    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemoveKeyManagerGrpcServiceGrpc.RemoveKeyManagerGrpcServiceBlockingStub? {
            return RemoveKeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}