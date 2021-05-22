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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import java.util.UUID.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class BuscarChavePixServerTest(
        @Inject val chaveRepository: ChaveRepository,
        @Inject val grpcClient: SearchPixKeyManagerGrpcServiceGrpc.SearchPixKeyManagerGrpcServiceBlockingStub
) {
    @Inject
    lateinit var client: BancoCentralBrasilClient
    val IDCLIENT = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        chaveRepository.deleteAll()
    }

    //Key manager
    @Test
    fun `nao deve consultar com entradas invalidas`() {
        val requestInvalida = SearchKeyRequest.getDefaultInstance()
        val requestIdPortadorInvalido = SearchKeyRequest.newBuilder().setPixId(randomUUID().toString()).build()
        val requestPixIDInvalido = SearchKeyRequest.newBuilder().setIdPortador(randomUUID().toString()).build()
        val requestsInvalidas = listOf(requestInvalida, requestIdPortadorInvalido, requestPixIDInvalido)
        requestsInvalidas.forEach { request ->

            val e = assertThrows<StatusRuntimeException> {
                grpcClient.consultarChaveKeyManager(request)
            }
            with(e) {
                assertEquals(Status.INVALID_ARGUMENT, status.code.toStatus())
                assertEquals("PixId e/ou IdPortador devem ser preenchidos.", status.description)
            }
        }

    }

    @Test
    fun `nao deve retornar uma chave que nao existe`() {
        val request = SearchKeyRequest.newBuilder().setPixId(randomUUID().toString()).setIdPortador(randomUUID().toString()).build()

        val e = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChaveKeyManager(request)
        }
        with(e) {
            assertEquals(Status.NOT_FOUND, status.code.toStatus())
            assertEquals("Chave Pix não existente.", status.description)
        }
    }

    @Test
    fun `deve retornar a chave com uma request valida`() {
        val chave = Chave(chave = "16657363052", idPortador = IDCLIENT, tipoChave = TipoDaChave.CPF, conta = TipoDaConta.CONTA_POUPANCA, contaAssociada = dadosResponse().paraContaAssociada())
        chaveRepository.save(chave)
        val resposta = chave.paraSearchKeyReponse()

        val request = SearchKeyRequest.newBuilder().setPixId(chave.id).setIdPortador(IDCLIENT).build()
        val response = grpcClient.consultarChaveKeyManager(request)
        with(response) {
            assertEquals(resposta.idPix, this.idPix)
            assertEquals(resposta.idPortador, this.idPortador)
            assertEquals(resposta.chave, this.chave)
            assertEquals(resposta.tipoChave, this.tipoChave)
            assertEquals(resposta.conta, this.conta)
            assertEquals(resposta.cpfPortador, this.cpfPortador)
            assertEquals(resposta.nomePortador, this.nomePortador)


        }
    }

    //external services
    @Test
    fun `nao dever consultar com a chave invalida`() {
        val request = SearchKeyExternalRequest.getDefaultInstance()
        val e = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChaveExternal(request)
        }
        with(e) {
            assertEquals(Status.INVALID_ARGUMENT, this.status.code.toStatus())
            assertEquals("O campo chave deve ser preenchido.", this.status.description)
        }
    }

    @Test
    fun `nao dever consultar com a chave em formato invalido`() {
        val request = SearchKeyExternalRequest.newBuilder().setChave(randomUUID().toString()).build()
        val e = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChaveExternal(request)
        }
        with(e) {
            assertEquals(Status.INVALID_ARGUMENT, this.status.code.toStatus())
            assertEquals("Chave em formato invalido.", this.status.description)
        }
    }

    @Test
    fun `nao deve retornar uma chave que nao existe no keymanager e nem no bcb`() {
        val request = SearchKeyExternalRequest.newBuilder().setChave("95441068043").build()
        `when`(client.buscarChave(request.chave)).thenReturn(HttpResponse.notFound())
        val e = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChaveExternal(request)
        }
        with(e) {
            assertEquals(Status.NOT_FOUND, this.status.code.toStatus())
            assertEquals("Chave Pix não existente.", this.status.description)
        }
    }

    @Test
    fun `deve retornar uma chave que nao existe no keymanager mas existe no bcb`() {
        val chave = Chave(chave = "16657363052", idPortador = IDCLIENT, tipoChave = TipoDaChave.CPF, conta = TipoDaConta.CONTA_POUPANCA, contaAssociada = dadosResponse().paraContaAssociada())
        val request = SearchKeyExternalRequest.newBuilder().setChave("16657363052").build()
        `when`(client.buscarChave(request.chave)).thenReturn(HttpResponse.ok(dadosBancoCentralResponse(chave.paraCreatePixRequest())))
        val resposta = chave.paraSearchKeyExternalResponse()
        val response = grpcClient.consultaChaveExternal(request)

        with(response) {

            assertEquals(resposta.chave, this.chave)
            assertEquals(resposta.tipoChave, this.tipoChave)
            assertEquals(resposta.conta, this.conta)
            assertEquals(resposta.cpfPortador, this.cpfPortador)
            assertEquals(resposta.nomePortador, this.nomePortador)


        }
    }

    @Test
    fun `deve retornar uma chave que existe no keymanager sem consultar o  bcb`() {
        val chave = Chave(chave = "16657363052", idPortador = IDCLIENT, tipoChave = TipoDaChave.CPF, conta = TipoDaConta.CONTA_POUPANCA, contaAssociada = dadosResponse().paraContaAssociada())
        chaveRepository.save(chave)
        val request = SearchKeyExternalRequest.newBuilder().setChave("16657363052").build()

        val resposta = chave.paraSearchKeyExternalResponse()
        val response = grpcClient.consultaChaveExternal(request)

        with(response) {

            assertEquals(resposta.chave, this.chave)
            assertEquals(resposta.tipoChave, this.tipoChave)
            assertEquals(resposta.conta, this.conta)
            assertEquals(resposta.cpfPortador, this.cpfPortador)
            assertEquals(resposta.nomePortador, this.nomePortador)


        }
    }
    //listar chaves de um portador
    @Test
    fun `nao dever consultar todas as chaves se o id do cliente for nulo`() {
        val request = SearchAllKeyBearerRequest.getDefaultInstance()
        val e = assertThrows<StatusRuntimeException> {
            grpcClient.listarTodasChavesDoPortador(request)
        }
        with(e) {
            assertEquals(Status.INVALID_ARGUMENT, this.status.code.toStatus())
            assertEquals("IdPortador deve ser preenchido.", this.status.description)
        }
    }
    @Test
    fun `dever retornar uma lista vazia se o portador nao tiver chaves`() {
        val request = SearchAllKeyBearerRequest.newBuilder().setIdPortador(IDCLIENT).build()

        val response = grpcClient.listarTodasChavesDoPortador(request)
        with(response) {
            assertEquals(SearchAllKeyBearerResponse.getDefaultInstance(), this)

        }
    }
    @Test
    fun `dever retornar a lista de chaves do portador `() {
        val chaves =retornaListadeChaves()
        chaveRepository.saveAll(chaves)
        val keys=chaves.map { c->c.paraKey() }

        val request = SearchAllKeyBearerRequest.newBuilder().setIdPortador(IDCLIENT).build()

        val response = grpcClient.listarTodasChavesDoPortador(request)
        response.keysList.forEachIndexed() { index,c->
            with(c){
                assertEquals(keys[index].chave,this.chave)
                assertEquals(keys[index].idPix,this.idPix)
                assertEquals(keys[index].idPortador,this.idPortador)
                assertEquals(keys[index].tipoChave,this.tipoChave)
                assertEquals(keys[index].tipoConta,this.tipoConta)

            }

        }

    }

    @MockBean(BancoCentralBrasilClient::class)
    fun bancoCentral(): BancoCentralBrasilClient? {
        return Mockito.mock(BancoCentralBrasilClient::class.java)
    }

//    @Factory
//    class Clients {
//       @Replaces(bean=SearchPixKeyManagerGrpcServiceGrpc.SearchPixKeyManagerGrpcServiceBlockingStub::class)
////        @Singleton
//      //  @stubTest
//        @Prototype
//        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): SearchPixKeyManagerGrpcServiceGrpc.SearchPixKeyManagerGrpcServiceBlockingStub {
//            return SearchPixKeyManagerGrpcServiceGrpc.newBlockingStub(channel)
//        }
//    }
    private fun retornaListadeChaves():List<Chave>{
        val cpf = Chave(chave = "16657363052", idPortador = IDCLIENT, tipoChave = TipoDaChave.CPF, conta = TipoDaConta.CONTA_POUPANCA, contaAssociada = dadosResponse().paraContaAssociada())
        val email = Chave(chave = "jordi@email.com", idPortador = IDCLIENT, tipoChave = TipoDaChave.EMAIL, conta = TipoDaConta.CONTA_POUPANCA, contaAssociada = dadosResponse().paraContaAssociada())
        return listOf(cpf,email)
    }
    private fun dadosResponse(): ItauLegacyClient.ResponseItau {
        val titular = ItauLegacyClient.Titular("Jordi Henrique Marques da Silva", IDCLIENT, "32759160092")
        val instuicao = ItauLegacyClient.Instituicao("ITAÚ UNIBANCO S.A.", "45612")
        return ItauLegacyClient.ResponseItau(TipoDaConta.CONTA_POUPANCA.name, "0001", "12356", titular, instuicao)

    }

    private fun dadosBancoCentralResponse(chave: BancoCentralBrasilClient.CriarChaveRequest): BancoCentralBrasilClient.CriarChaveResponse {
        return BancoCentralBrasilClient.CriarChaveResponse(
                key = chave.key,
                keyType = chave.keyType,
                bankAccount = chave.bankAccount,
                owner = chave.owner,
                createdAt = LocalDateTime.now()
        )
    }
}

