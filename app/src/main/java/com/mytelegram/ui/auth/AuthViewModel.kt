package com.mytelegram.ui.auth

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mytelegram.data.model.MainUser
import com.mytelegram.data.model.resouces.ConnectionStatus
import com.mytelegram.data.model.resouces.EventConnectionStatus
import com.mytelegram.data.model.resouces.EventResource
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.data.repository.AuthRepository
import com.mytelegram.ui.base.BaseViewModel
import io.socket.client.Socket

class AuthViewModel @ViewModelInject constructor(private val authRepository: AuthRepository) :
    BaseViewModel() {
    override fun onCleared() {
        io { authRepository.disconnectFromAuthServer() }
        super.onCleared()
    }

    private val _connectionStatus =
        MutableLiveData(EventConnectionStatus(ConnectionStatus.Disconnect))
    val connectionStatus: LiveData<EventConnectionStatus> get() = _connectionStatus

    fun connectToAuthServer(token: String) = io {
        authRepository.connectToAuthServer(token)?.also { socket ->
            socket.on(Socket.EVENT_CONNECT) {
                _connectionStatus.postValue(EventConnectionStatus(ConnectionStatus.Connect))
            }
            socket.on(Socket.EVENT_ERROR) { args ->
                if (args[0] != null && args[0].toString().startsWith("ae:"))
                    _connectionStatus.postValue(EventConnectionStatus(ConnectionStatus.Error(true)))
                else
                    _connectionStatus.postValue(EventConnectionStatus(ConnectionStatus.Error(false)))
            }
            socket.on(Socket.EVENT_DISCONNECT) {
                _connectionStatus.postValue(EventConnectionStatus(ConnectionStatus.Disconnect))
            }
        }
    }

    private val _loginResponse = MutableLiveData<EventResource<Boolean>>()
    val loginResponse: LiveData<EventResource<Boolean>> get() = _loginResponse


    fun login(phoneNumber: String, countryCode: String) = io {
        _loginResponse.postValue(EventResource(Resource.Loading))
        _loginResponse.postValue(EventResource(authRepository.login(phoneNumber, countryCode)))
    }

    private val _codeValidationResponse = MutableLiveData<EventResource<MainUser>>()
    val codeValidationResponse: LiveData<EventResource<MainUser>> get() = _codeValidationResponse

    fun codeValidation(phoneNumber: String, token: String) = io {
        _loginResponse.postValue(EventResource(Resource.Loading))
        _codeValidationResponse.postValue(
            EventResource(authRepository.codeValidation(phoneNumber, token))
        )
    }

    fun saveUser(mainUser: MainUser) =
        io { authRepository.saveMainUser(mainUser) }

}

