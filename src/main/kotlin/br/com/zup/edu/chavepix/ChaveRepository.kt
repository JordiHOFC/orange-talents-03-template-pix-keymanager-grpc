package br.com.zup.edu.chavepix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChaveRepository:JpaRepository<Chave,String> {
    fun existsByChave(chave: String): Boolean
    fun findByChave(chave: String): Optional<Chave>
    fun findByIdPortador(idPortador:String): List<Chave>

}