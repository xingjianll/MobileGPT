package com.example.mobilegpt.viewmodel

import com.example.mobilegpt.socket.ClientSocket

object global {
    var peerPort: Int = 4396
//    10.0.2.2
    var peerAddr: String = "13.55.198.181"
    var selfPort: Int? = null
    const val LOGIN: String = "00"
    const val CHAT: String = "01"
    lateinit var clientSocket: ClientSocket
}