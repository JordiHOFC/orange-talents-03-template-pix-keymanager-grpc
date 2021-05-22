package br.com.zup.edu.chavepix

import br.com.zup.edu.*
import br.com.zup.edu.clients.BancoCentralBrasilClient
import br.com.zup.edu.clients.ItauLegacyClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Prototype
import io.micronaut.context.annotation.Replaces
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
import java.time.LocalDateTime
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
    lateinit var clientItau: ItauLegacyClient

    @Inject
    lateinit var clientBcb: BancoCentralBrasilClient

    @Test
    internal fun `deve cadastrar uma chave valida`() {
        val request = PixKeyRequest.newBuilder()
                .setIdPortador(IDCLIENT)
                .setTipo(TipoChave.CPF)
                .setConta(TipoConta.CONTA_POUPANCA)
                .setChave("32759160092")
                .build()
        val chave = request.paraChavePixRequest().paraChave(dadosResponse().paraContaAssociada())
        `when`(clientItau.findClient(IDCLIENT, TipoConta.CONTA_POUPANCA.name)).thenReturn(HttpResponse.ok(dadosResponse()))
        `when`(clientBcb.cadastrarChave(chave.paraCreatePixRequest())).thenReturn(HttpResponse.created(dadosBancoCentralResponse(chave.paraCreatePixRequest())))

        val reponse = grpcClient.cadastrarChave(request)
        with(reponse) {
            assertNotNull(idPix)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma chave quando o cliente nao existe no itau`() {
        `when`(clientItau.findClient(IDCLIENT, TipoConta.CONTA_POUPANCA.name)).thenReturn(HttpResponse.notFound())

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
            assertEquals("Nao exite cadastro para este cliente.", e.status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma chave quando o servidor do Banco central esta fora`() {
        val request = PixKeyRequest.newBuilder()
                .setIdPortador(IDCLIENT)
                .setTipo(TipoChave.CPF)
                .setConta(TipoConta.CONTA_POUPANCA)
                .setChave("32759160092")
                .build()
        val chave = request.paraChavePixRequest().paraChave(dadosResponse().paraContaAssociada())
        `when`(clientItau.findClient(IDCLIENT, TipoConta.CONTA_POUPANCA.name)).thenReturn(HttpResponse.ok(dadosResponse()))
        `when`(clientBcb.cadastrarChave(chave.paraCreatePixRequest())).thenReturn(HttpResponse.notFound())

        val e = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }

        with(e) {
            assertEquals(Status.FAILED_PRECONDITION, e.status.code.toStatus())
            assertEquals("Falha ao sincronizar ao Banco Central do Brasil.", e.status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma chave em formato invalido`() {

        val request = PixKeyRequest.newBuilder()
                .setIdPortador(IDCLIENT)
                .setTipo(TipoChave.CPF)
                .setConta(TipoConta.CONTA_POUPANCA)
                .setChave("32aasa160092")
                .build()
        val e = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }
        with(e) {
            assertEquals(Status.INVALID_ARGUMENT, e.status.code.toStatus())
            assertEquals("Chave em formato Invalido.", e.status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma chave com todos dados em branco`() {

        val request = PixKeyRequest.getDefaultInstance()
        val e = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }
        with(e) {
            assertEquals(Status.INVALID_ARGUMENT, e.status.code.toStatus())
            assertEquals("Chave em formato Invalido.", e.status.description)
        }
    }


    @Test
    internal fun `nao deve cadastrar uma chave ja existente`() {
        val chave = Chave(chave = "16657363052", idPortador = IDCLIENT, tipoChave = TipoDaChave.CPF, conta = TipoDaConta.CONTA_POUPANCA, contaAssociada = dadosResponse().paraContaAssociada())
        repository.save(chave)
        val request = PixKeyRequest.newBuilder()
                .setIdPortador(IDCLIENT)
                .setTipo(TipoChave.CPF)
                .setConta(TipoConta.CONTA_POUPANCA)
                .setChave("16657363052")
                .build()
        val e = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }
        with(e) {
            assertEquals(Status.ALREADY_EXISTS, e.status.code.toStatus())
            assertEquals("Chave já cadastrada.", e.status.description)
        }
    }


    @MockBean(ItauLegacyClient::class)
    fun contasItau(): ItauLegacyClient? {
        return Mockito.mock(ItauLegacyClient::class.java)
    }

    @MockBean(BancoCentralBrasilClient::class)
    fun bancoCentral(): BancoCentralBrasilClient? {
        return Mockito.mock(BancoCentralBrasilClient::class.java)
    }

//    @Factory
//    class Clients {
//        @Replaces(bean= PixKeyManagerGRpcServiceGrpc.PixKeyManagerGRpcServiceBlockingStub::class)
////        @Singleton
//        @Prototype
//        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerGRpcServiceGrpc.PixKeyManagerGRpcServiceBlockingStub? {
//            return PixKeyManagerGRpcServiceGrpc.newBlockingStub(channel)
//        }
//   }

    private fun dadosResponse(): ItauLegacyClient.ResponseItau {
        val titular = ItauLegacyClient.Titular("Jordi Henrique Marques da Silva", IDCLIENT, "32759160092")
        val instuicao = ItauLegacyClient.Instituicao("Uni Banco Itau SA", "45612")
        return ItauLegacyClient.ResponseItau(TipoDaConta.CONTA_POUPANCA.name, "0001", "12356", titular, instuicao)

    }
    private fun dadosBancoCentralResponse(chave:BancoCentralBrasilClient.CriarChaveRequest):BancoCentralBrasilClient.CriarChaveResponse{
        return BancoCentralBrasilClient.CriarChaveResponse(
                key = chave.key,
                keyType = chave.keyType,
                bankAccount = chave.bankAccount,
                owner = chave.owner,
                createdAt = LocalDateTime.now()
        )
    }
}
