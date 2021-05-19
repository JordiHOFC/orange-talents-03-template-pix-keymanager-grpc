package br.com.zup.edu.exceptions

import io.grpc.Status

class NaoPortadorDaChaveException(val mensagem: String):ExceptionGRPC() {
    override fun getException(): Status {
        return Status.FAILED_PRECONDITION.withDescription(mensagem)
    }
}