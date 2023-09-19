package com.example.mobilegpt.socket

interface ClientSocketUser
{
    fun onMsg(head: String, body: String)
}

