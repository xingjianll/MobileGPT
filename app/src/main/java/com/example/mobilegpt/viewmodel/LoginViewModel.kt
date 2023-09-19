package com.example.mobilegpt.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilegpt.socket.ClientSocket
import com.example.mobilegpt.socket.ClientSocketUser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginViewModel : ViewModel(), ClientSocketUser {

    private val _loginEvent = MutableSharedFlow<LogInEvent>()
    private val _loadingState = MutableLiveData<UiLoadingState>()
    val loginEvent = _loginEvent.asSharedFlow()
    val loadingState : LiveData<UiLoadingState>
        get() = _loadingState

    private var clientSocket : ClientSocket? = null

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            if (username.trim().isNotEmpty() && password.trim().isNotEmpty()) {
                loginRegisteredUser(username, password)
            } else {
                _loginEvent.emit(LogInEvent.ErrorInputTooShort)
            }
        }
    }

    fun addSocket(clientSocket : ClientSocket){
        this.clientSocket = clientSocket
    }

    private fun loginRegisteredUser(username: String, password: String) {
        Log.d("MyApp", "loginRegisteredUser")
        runBlocking {
            try{clientSocket?.send(global.LOGIN, username + 30.toChar() + password)}
            catch(_:UninitializedPropertyAccessException){}
        }
    }

    sealed class LogInEvent {
        object ErrorInputTooShort : LogInEvent()
        data class ErrorLogIn(val error: String): LogInEvent()
        object Success : LogInEvent()
        object Failure : LogInEvent()
    }

    sealed class UiLoadingState {
        object Loading : UiLoadingState()
        object NotLoading : UiLoadingState()
    }

    override fun onMsg(head: String, body: String) {
        Log.d("MyApp", head)
        if (head == global.LOGIN) {
            if (body == "0"){
                viewModelScope.launch {
                    _loginEvent.emit(LogInEvent.Success)
                }
            }
            else{
                viewModelScope.launch {
                    _loginEvent.emit(LogInEvent.Failure)
                }
            }
        }
    }
}