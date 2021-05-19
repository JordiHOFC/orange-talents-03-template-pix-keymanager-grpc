package br.com.zup.edu.chavepix

import br.com.zup.edu.PixKeyManagerGRpcServiceGrpc
import br.com.zup.edu.PixKeyRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.clients.ItauLegacyClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastrarNovaChavePixServerTest(
        @Inject val repository: ChaveRepository,
        @Inject val grpcClient: PixKeyManagerGRpcServiceGrpc.PixKeyManagerGRpcServiceBlockingStub
) {
    val IDCLIENT = UUID.randomUUID().toString()

    @Inject
    lateinit var client: ItauLegacyClient


    @Test
    internal fun `deve cadastrar uma chave valida`() {

        `when`(client.findClient(IDCLIENT, TipoConta.CONTA_POUPANCA.name)).thenReturn(HttpResponse.ok(dadosResponse()))

        val request = PixKeyRequest.newBuilder()
                .setIdPortador(IDCLIENT)
                .setTipo(TipoChave.CPF)
                .setConta(TipoConta.CONTA_POUPANCA)
                .setChave("32759160092")
                .build()

        val reponse = grpcClient.cadastrarChave(request)
        with(reponse) {
            assertNotNull(idPix)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma chave quando o cliente nao existe no itau`() {
        `when`(client.findClient(IDCLIENT, TipoConta.CONTA_POUPANCA.name)).thenReturn(HttpResponse.notFound())

        val request = PixKeyRequest.newBuilder()
                .setIdPortador(IDCLIENT)
                .setTipo(TipoChave.CPF)
                .setConta(TipoConta.CONTA_POUPANCA)
                .setChave("32759160092")
                .build()
        val e = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }

        with(e) {
            assertEquals(Status.FAILED_PRECONDITION, e.status.code.toStatus())
            assertEquals("Nao exite cadastro para este cliente.",e.status.description)
        }
    }

    @MockBean(ItauLegacyClient::class)
    fun contasItau(): ItauLegacyClient? {
        return Mockito.mock(ItauLegacyClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerGRpcServiceGrpc.PixKeyManagerGRpcServiceBlockingStub? {
            return PixKeyManagerGRpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun dadosResponse(): ItauLegacyClient.ResponseItau {
        val titular = ItauLegacyClient.Titular("Jordi Henrique Marques da Silva", IDCLIENT, "32759160092")
        val instuicao = ItauLegacyClient.Instituicao("Uni Banco Itau SA", "45612")
        return ItauLegacyClient.ResponseItau(TipoDaConta.CONTA_POUPANCA.name, "0001", "12356", titular, instuicao)

    }
}
