package br.com.zup.edu.exceptions

import io.grpc.Status

class ChavePixExistenteException(val s: String) : ExceptionGRPC() {

    override fun getException():Status{
        return Status.ALREADY_EXISTS.withDescription(s)
    }

}
