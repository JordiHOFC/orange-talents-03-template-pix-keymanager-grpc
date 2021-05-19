package br.com.zup.edu.exceptions

import io.grpc.Status

class ChavePixNotFoundException(val mensagem:String): ExceptionGRPC() {
    override fun getException(): Status {
        return Status.NOT_FOUND.withDescription(mensagem)
    }
}