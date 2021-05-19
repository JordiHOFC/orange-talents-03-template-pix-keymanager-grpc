package br.com.zup.edu.chavepix

import br.com.zup.edu.RemoveKeyManagerGrpcServiceGrpc
import br.com.zup.edu.RemoveKeyRequest
import br.com.zup.edu.RemoveKeyResponse
import br.com.zup.edu.clients.ItauLegacyClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoverChavePixServerTest(
        val chaveRepository: ChaveRepository,
        @Inject val grpcClient: RemoveKeyManagerGrpcServiceGrpc.RemoveKeyManagerGrpcServiceBlockingStub
) {
    val IDCLIENT :String= UUID.randomUUID().toString()

    @BeforeEach
    fun setup(){
        chaveRepository.deleteAll()
    }
    @Test
    fun `deve remover uma chave caso a request seja valida`(){
        val chave = Chave(chave = "16657363052", idPortador = IDCLIENT, tipoChave = TipoDaChave.CPF, conta = TipoDaConta.CONTA_POUPANCA, contaAssociada = dadosResponse().paraContaAssociada())
        chaveRepository.save(chave)
        val request = RemoveKeyRequest.newBuilder()
                .setPixId(chave.id)
                .setIdPortador(IDCLIENT)
                .build()
        val response = grpcClient.removerChave(request)
        with(response){
            assertEquals(RemoveKeyResponse.getDefaultInstance(),this)
        }
    }


    @Test
    fun `somento o dono da chave pode remove-la`(){
        val chave = Chave(chave = "16657363052", idPortador = IDCLIENT, tipoChave = TipoDaChave.CPF, conta = TipoDaConta.CONTA_POUPANCA, contaAssociada = dadosResponse().paraContaAssociada())
        chaveRepository.save(chave)
        val request = RemoveKeyRequest.newBuilder()
                .setPixId(chave.id)
                .setIdPortador(UUID.randomUUID().toString())
                .build()
        val e = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }
        with(e) {
            assertEquals(Status.FAILED_PRECONDITION, e.status.code.toStatus())
            assertEquals("Somente o portador da chave tem premissao para remove-la.", e.status.description)
        }
    }

    @Test
    fun `nao deve remover uma chave que nao existe`(){
        val request = RemoveKeyRequest.newBuilder()
                .setPixId(UUID.randomUUID().toString())
                .setIdPortador(UUID.randomUUID().toString())
                .build()
        val e = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }
        with(e) {
            assertEquals(Status.NOT_FOUND, e.status.code.toStatus())
            assertEquals("Esta chave inexistente.", e.status.description)
        }

    }


    @Test
    fun `nao deve remover uma chave com dados invalido`() {
        val defaultInstance = RemoveKeyRequest.getDefaultInstance()
        val requestSemPortador = RemoveKeyRequest.newBuilder().setIdPortador("").setPixId(UUID.randomUUID().toString()).build()
        val requestSemPixID = RemoveKeyRequest.newBuilder().setPixId("").setIdPortador(UUID.randomUUID().toString()).build()
        val listOf = listOf(defaultInstance, requestSemPixID, requestSemPortador)
        listOf.forEach { request ->
            val e = assertThrows<StatusRuntimeException> {
                grpcClient.removerChave(request)
            }
            with(e) {
                assertEquals(Status.INVALID_ARGUMENT, e.status.code.toStatus())
                assertEquals("IdPix e/ou IdPortador devem ser preenchidos.", e.status.description)
            }

        }
    }


    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemoveKeyManagerGrpcServiceGrpc.RemoveKeyManagerGrpcServiceBlockingStub? {
            return RemoveKeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun dadosResponse(): ItauLegacyClient.ResponseItau {
        val titular = ItauLegacyClient.Titular("Jordi Henrique Marques da Silva", IDCLIENT, "32759160092")
        val instuicao = ItauLegacyClient.Instituicao("Uni Banco Itau SA", "45612")
        return ItauLegacyClient.ResponseItau(TipoDaConta.CONTA_POUPANCA.name, "0001", "12356", titular, instuicao)

    }
}