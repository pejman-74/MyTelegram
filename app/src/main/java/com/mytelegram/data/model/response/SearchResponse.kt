package com.mytelegram.data.model.response


import com.google.gson.annotations.SerializedName
import com.mytelegram.data.model.Room
import com.mytelegram.data.model.User

data class SearchResponse(
        @SerializedName("rooms")
        val rooms: List<Room>,
        @SerializedName("users")
        val users: List<User>
)