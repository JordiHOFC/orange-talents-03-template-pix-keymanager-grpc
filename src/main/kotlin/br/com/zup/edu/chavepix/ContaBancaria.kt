package br.com.zup.edu.chavepix

import javax.persistence.*

@Embeddable
class ContaAssociada(
        val nome: String,
        val cpf: String,
        val agencia: String,
        val numero: String,
        val nomeInstituicao:String,
        val ispbInstituicao:String
        ){}