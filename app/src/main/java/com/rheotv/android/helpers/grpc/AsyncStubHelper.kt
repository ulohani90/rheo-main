package com.rheotv.android.helpers.grpc

import com.rheotv.android.BuildConfig
import goChat.ChatGrpc
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import java.util.concurrent.TimeUnit

object AsyncStubHelper {
    private var asyncStub: ChatGrpc.ChatStub? = null

    fun getGlobalStub(): ChatGrpc.ChatStub {
        if (asyncStub == null) {
            val channel = ManagedChannelBuilder.forAddress(BuildConfig.CHAT_URL, BuildConfig.CHAT_PORT)
                    .keepAliveTime(10000, TimeUnit.MILLISECONDS)
                    .keepAliveTimeout(5000, TimeUnit.MILLISECONDS)
                    .keepAliveWithoutCalls(true).usePlaintext().build()
            asyncStub = ChatGrpc.newStub(channel)
            val header = Metadata()
            val key = Metadata.Key.of("app-version", Metadata.ASCII_STRING_MARSHALLER)
            header.put<String?>(key, BuildConfig.VERSION_CODE.toString())
            asyncStub = MetadataUtils.attachHeaders(asyncStub, header)
        }
        return asyncStub!!
    }

}