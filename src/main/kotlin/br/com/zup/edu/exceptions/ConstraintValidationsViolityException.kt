package br.com.zup.edu.exceptions

import io.grpc.Status

class ConstraintValidationsViolityException(val s: String) : ExceptionGRPC() {
    override fun getException(): Status {
        return Status.INVALID_ARGUMENT.withDescription(s)
    }

}
