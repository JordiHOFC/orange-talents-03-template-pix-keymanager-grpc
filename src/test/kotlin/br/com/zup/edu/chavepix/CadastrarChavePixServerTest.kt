package br.com.zup.edu.chavepix

import br.com.zup.edu.PixKeyManagerGRpcServiceGrpc
import br.com.zup.edu.PixKeyRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastrarChavePixServerTest(
    val grpcClient: PixKeyManagerGRpcServiceGrpc.PixKeyManagerGRpcServiceBlockingStub,
    val repository: ChaveRepository
) {
    @Test
    internal fun `deve cadastrar uma chave diferente de aleatoria valida`() {
        val request = PixKeyRequest.newBuilder()
            .setChave("+5534999451729")
            .setConta(TipoConta.CONTA_CORRENTE)
            .setIdPortador(UUID.randomUUID().toString())
            .setTipo(TipoChave.TELEFONECELULAR).build()

        var response = grpcClient.cadastrarChave(request)
        assertNotNull{response.idPix}

    }

    @Test
    internal fun `deve cadastrar uma chave aleatoria`() {
        val request = PixKeyRequest.newBuilder()
            .setConta(TipoConta.CONTA_CORRENTE)
            .setIdPortador(UUID.randomUUID().toString())
            .setTipo(TipoChave.CHAVEALEATORIA).build()

        var response = grpcClient.cadastrarChave(request)
        assertNotNull{response.idPix}
    }

    @Test
    internal fun `nao deve cadastrar uma chave ja existente`() {
        var request = PixKeyRequest.newBuilder()
            .setChave("09184709006")
            .setConta(TipoConta.CONTA_CORRENTE)
            .setTipo(TipoChave.CPF)
            .setIdPortador(UUID.randomUUID().toString())
            .build()
        val chave= request.paraChave()
        repository.save(chave)
        var e = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }

        assertEquals(Status.ALREADY_EXISTS, e.status.code.toStatus())
        assertEquals("Chave já cadastrada.", e.status.description)
    }

    @Test
    fun `nao deve cadastrar chave diferente de aleatoria com formato invalido`() {
        val request= PixKeyRequest.newBuilder()
            .setChave("jor.silva")
            .setConta(TipoConta.CONTA_POUPANCA)
            .setTipo(TipoChave.EMAIL)
            .setIdPortador(UUID.randomUUID().toString())
            .build()
        var e = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }
        assertEquals(Status.INVALID_ARGUMENT, e.status.code.toStatus())
        assertEquals("O valor informado não corresponde ao ${request.tipo.name}.", e.status.description)

    }

    @Test
    internal fun `nao deve cadastrar chave diferente de aleatoria com valor em branco`() {
        val request = PixKeyRequest.newBuilder()
            .setConta(TipoConta.CONTA_CORRENTE)
            .setIdPortador(UUID.randomUUID().toString())
            .setTipo(TipoChave.CPF).build()

        var e = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }

        assertEquals(Status.INVALID_ARGUMENT, e.status.code.toStatus())
        assertEquals("Deve conter um valor para chave, caso o tipo seja diferente de Aleatoria.", e.status.description)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerGRpcServiceGrpc.PixKeyManagerGRpcServiceBlockingStub? {
            return PixKeyManagerGRpcServiceGrpc.newBlockingStub(channel)
        }
    }
}

private fun PixKeyRequest.paraChave(): Chave {
    return Chave(this.chave,this.idPortador, TipoDaConta.valueOf(this.conta.name), TipoDaChave.valueOf(this.tipo.name))

}
