package com.thesamet.pb

import com.thesamet.proto.e2e.Service
import io.grpc.protobuf.ProtoMethodDescriptorSupplier
import io.grpc.{Context, Contexts, Metadata, ServerCall, ServerCallHandler, ServerInterceptor}

class Service1Interceptor extends ServerInterceptor {
  override def interceptCall[ReqT, RespT](
      call: ServerCall[ReqT, RespT],
      headers: Metadata,
      next: ServerCallHandler[ReqT, RespT]
  ): ServerCall.Listener[ReqT] = {
    val schemaDescriptor =
      call.getMethodDescriptor.getSchemaDescriptor.asInstanceOf[ProtoMethodDescriptorSupplier]

    val value = for {
      methodDescriptor <- Option(schemaDescriptor.getMethodDescriptor)
      options          <- Option(methodDescriptor.getOptions)
    } yield options.getExtension(Service.customOption)

    val newCtx =
      Context.current().withValue[String](Service1Interceptor.contextKey, value.getOrElse(""))
    Contexts.interceptCall(newCtx, call, headers, next)
  }
}

object Service1Interceptor {
  val contextKey = Context.key[String]("CUSTOM_OPTION")
}
