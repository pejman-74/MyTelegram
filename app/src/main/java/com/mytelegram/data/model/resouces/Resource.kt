package com.mytelegram.data.model.resouces


/*
* This sealed class uses to format api calls responses.
* */
sealed class Resource<out T> {
    data class Success<out T>(val value: T) : Resource<T>()
    data class Failure(val exception: Throwable) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

//BIND RESOURCE AND EVENT FOR MORE READABILITY.
class EventResource<out T>(content: Resource<T>) : Event<Resource<T>>(content)



