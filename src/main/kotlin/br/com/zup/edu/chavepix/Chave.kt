package br.com.zup.edu.chavepix

import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity
class Chave(
        @field:Column(unique = true, length = 77) @field:Size(max = 77) @field:NotBlank var chave: String,
        @field:Column(nullable = false) @field:NotBlank val idPortador: String,
        @field:Enumerated(EnumType.STRING) @field:NotBlank val conta: TipoDaConta,
        @field:Enumerated(EnumType.STRING) @field:NotBlank val tipoChave: TipoDaChave,
        @field:Embedded val contaAssociada: ContaAssociada,
        @field:CreationTimestamp var criadoEm: LocalDateTime = LocalDateTime.now()
) {
    @Id
    val id: String = UUID.randomUUID().toString()
    fun atualizaChave(chave: String) {
        this.chave
    }

    fun atualizaDataCriacao(dateTime: LocalDateTime) {
        this.criadoEm = dateTime
    }
}