package br.com.zup.edu.chavepix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
@Repository
interface ChaveRepository:JpaRepository<Chave,String> {
    fun existsByChave(chave: String):Boolean

}