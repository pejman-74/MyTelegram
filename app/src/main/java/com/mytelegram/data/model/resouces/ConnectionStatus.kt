package com.mytelegram.data.model.resouces

/*
* This sealed class uses to format Socket.io connection status.
* Errors can has two type :
*   1-isAuth, if it was true then message is empty.
*   1-isAuth, if it was false then message is have error details.
* */
sealed class ConnectionStatus {
    object Connect : ConnectionStatus()
    object Disconnect : ConnectionStatus()
    class Error(val isAuth: Boolean, val message: String? = null) : ConnectionStatus()
}
//BIND CONNECTION-STATUS AND EVENT FOR MORE READABILITY.
class EventConnectionStatus(content: ConnectionStatus) : Event<ConnectionStatus>(content)