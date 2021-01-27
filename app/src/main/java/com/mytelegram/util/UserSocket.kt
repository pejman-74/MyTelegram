package com.mytelegram.util

import io.socket.client.IO
import io.socket.client.Socket


class UserSocket {

    private var socket: Socket? = null

    fun connect(token: String): Socket? {
        if (socket == null)
            socket = IO.socket("http://192.168.1.6:3000", IO.Options().apply {
                path = "/sockets/user"
                query = "token=$token"
            })
        return socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
        socket=null
    }

    fun getSocket() = socket

}