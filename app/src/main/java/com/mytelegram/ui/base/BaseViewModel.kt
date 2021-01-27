package com.mytelegram.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    //lunch works in io dispatcher
    fun io(ioWork: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { ioWork.invoke() }
    }
}