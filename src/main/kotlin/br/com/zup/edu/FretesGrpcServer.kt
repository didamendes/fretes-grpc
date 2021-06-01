package br.com.zup.edu

import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer : FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesServiceGrpc::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {

        logger.info("Calculando frete para request: ${request}")

        val cep = request?.cep

        if (cep == null || cep.isBlank()) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("cep deve ser informado")
                .asRuntimeException()

            responseObserver?.onError(e)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("cep invalido")
                .augmentDescription("formato esperado deve ser 99999-999")
                .asRuntimeException()

            responseObserver?.onError(e)
        }

        // Simular um erro de seguranca
        if (cep.endsWith("333")) {

            val statusProto = com.google.rpc.Status.newBuilder()
                .setMessage("usuario nao pode acessar esse recurso")
                .setCode(Code.PERMISSION_DENIED.number)
                .addDetails(Any.pack(ErrorDetails.newBuilder()
                    .setCode(401)
                    .setMessage("Token inspirado")
                    .build()))
                .build()

            val e = io.grpc.protobuf.StatusProto.toStatusException(statusProto)
            responseObserver?.onError(e)
        }

        var valor = 0.0
        try {
            valor = Random.nextDouble(0.0, 140.0)

            if (valor > 100.0) {
                throw IllegalStateException("Erro inesperado ao executar logica de negocio")
            }

        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL.withDescription(e.message)
                .withCause(e)
                .asRuntimeException()
            )
        }

        val response = CalculaFreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(valor)
            .build()

        logger.info("Frete calculado: $response")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()

    }

}