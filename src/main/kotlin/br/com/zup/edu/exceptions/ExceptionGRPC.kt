package br.com.zup.edu.exceptions

import io.grpc.Status

abstract class ExceptionGRPC: Exception() {
   abstract fun getException():Status
}