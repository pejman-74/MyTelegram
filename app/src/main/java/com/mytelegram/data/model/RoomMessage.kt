package com.mytelegram.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class RoomMessage(
        @SerializedName("messageOwner")
        val messageOwner: String,
        @SerializedName("text")
        val text: String,
        @SerializedName("userCreateTime")
        @PrimaryKey(autoGenerate = false)
        val userCreateTime: String,
        @SerializedName("createdAt")
        val serverCreateTime: String?=null,
        @SerializedName("_id")
        val messageId: String? = null,
        @SerializedName("roomOwner")
        val roomOwner: String? = null
)