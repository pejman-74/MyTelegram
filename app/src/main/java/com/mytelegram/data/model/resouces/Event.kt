package com.mytelegram.data.model.resouces

import java.util.concurrent.atomic.AtomicBoolean

/*
* For handle one time events.(mostly uses of livedata observation)
*
* ***WE CAN USE CHANNELS, BUT FOR NOW USED THIS***
* */
open class Event<out T>(private val content: T) {

    private val hasBeenHandled = AtomicBoolean(false)

    fun getContentIfNotHandled(handleContent: (T) -> Unit) {
        if (!hasBeenHandled.get()) {
            hasBeenHandled.set(true)
            handleContent(content)
        }
    }

    fun peekContent() = content
    fun isHasBeenHandled() = hasBeenHandled
}


