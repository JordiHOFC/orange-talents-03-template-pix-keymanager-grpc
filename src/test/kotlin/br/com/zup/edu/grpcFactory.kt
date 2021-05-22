package br.com.zup.edu

import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel

@Factory
class grpcFactory {
    @Bean
    fun registeStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)=PixKeyManagerGRpcServiceGrpc.newBlockingStub(channel)
    @Bean
    fun removeStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)=RemoveKeyManagerGrpcServiceGrpc.newBlockingStub(channel)
    @Bean
    fun searchStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)=SearchPixKeyManagerGrpcServiceGrpc.newBlockingStub(channel)

}