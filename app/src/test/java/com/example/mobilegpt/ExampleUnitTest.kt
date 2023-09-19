package com.example.mobilegpt

import android.util.Log
import com.example.mobilegpt.activity.client
import com.example.mobilegpt.socket.ClientSocket
import com.example.mobilegpt.socket.ClientSocketUser
import com.example.mobilegpt.viewmodel.global
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*
import java.net.Socket

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun send_msg() {
        global.peerAddr = "127.0.0.1"
        var dummy = DummyUser()
        var soc = ClientSocket(mutableListOf(dummy))
        runBlocking {
            soc.setUp()
        }
        GlobalScope.launch (Dispatchers.IO){
            soc.listen()
        }
        runBlocking {
            print("sent")
            soc.send("00", "hello")
        }
        Thread.sleep(4000)
    }

    @Test
    fun test_global() {
        println(global.peerAddr)
        global.peerAddr = "3"
        println(global.peerAddr)
    }

    @Test
    fun toChar() {
        println(58.toChar())
    }

    @Test
    fun coroutineTest() {
        var a = 1
        GlobalScope.launch {
            a = 2
        }
        println(a)

        Thread.sleep(2000L)
        println(a)
    }

}

class DummyUser : ClientSocketUser {
    override fun onMsg(head: String, body: String) {
        println("hello")
    }
}