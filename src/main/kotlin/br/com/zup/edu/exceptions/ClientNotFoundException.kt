package br.com.zup.edu.exceptions

import io.grpc.Status

class ClientNotFoundException(val s: String) : ExceptionGRPC() {

    override fun getException(): Status {
        return Status.FAILED_PRECONDITION.withDescription(s)
    }
}
