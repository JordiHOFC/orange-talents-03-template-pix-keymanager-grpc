package br.com.zup.edu.handler

import br.com.zup.edu.chavepix.ServerGRPC
import br.com.zup.edu.exceptions.ExceptionGRPC
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
@InterceptorBean(ErrorHandler::class)
class ExceptionHandler : MethodInterceptor<ServerGRPC, Any?> {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun intercept(context: MethodInvocationContext<ServerGRPC, Any?>): Any? {

        try {
            context.proceed()

        } catch (e: ExceptionGRPC) {
            LOGGER.info(e.message)
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(e.getException().asRuntimeException())
        }
        return null
    }

}