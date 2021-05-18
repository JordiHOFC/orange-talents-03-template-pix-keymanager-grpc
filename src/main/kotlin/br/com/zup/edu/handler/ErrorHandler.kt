package br.com.zup.edu.handler

import io.micronaut.aop.Around

@Target(AnnotationTarget.CLASS,AnnotationTarget.FIELD,AnnotationTarget.TYPE)
@Around
@MustBeDocumented
annotation class ErrorHandler