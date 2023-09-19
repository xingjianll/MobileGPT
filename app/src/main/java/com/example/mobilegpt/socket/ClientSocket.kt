package com.example.mobilegpt.socket

import android.util.Base64OutputStream
import android.util.Log
import com.example.mobilegpt.viewmodel.LoginViewModel
import com.example.mobilegpt.viewmodel.global
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.InternalAPI
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.readFully
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.readUntilDelimiter
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import java.lang.StringBuilder
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.Scanner

class ClientSocket(private var observers: MutableList<ClientSocketUser>){
    lateinit var socket : io.ktor.network.sockets.Socket
    lateinit var input : ByteReadChannel
    lateinit var output: ByteWriteChannel

    @OptIn(InternalAPI::class)
    suspend fun setUp(){
        val selectorManager = SelectorManager(Dispatchers.IO)
        socket = aSocket(selectorManager).tcp().connect(global.peerAddr, global.peerPort)
        input = socket.openReadChannel()
        output = socket.openWriteChannel(autoFlush = true)
    }

    suspend fun send(head: String, body: String){
        output.writeStringUtf8(head+body)
    }

    fun addObserver(observer: ClientSocketUser){
        observers.add(observer)
    }

    suspend fun listen() {
        val sb = StringBuilder()
        while (true) {
            val x = input.readUTF8Line()
            if(x == "END4321") {
                val temp = sb.toString()
                sb.clear()
                Log.d("MyApp", temp)
                for (observer in observers)
                {
                    observer.onMsg(temp.substring(0,2), temp.substring(2))
                }
            }
            else{
                if (sb.toString() != ""){
                    sb.append("\n")
                }
                sb.append(x)
            }
        }
    }

    fun close(){
        socket.close()
    }
}