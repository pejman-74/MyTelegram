package com.mytelegram.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class PersonMessage(
    @SerializedName("_id")
    val messageId: String? = null,
    @SerializedName("messageOwner")
    val messageOwner: String? = null,
    @SerializedName("receiverUser")
    val receiverUser: String? = null,
    @SerializedName("text")
    val text: String,
    @SerializedName("userCreateTime")
    @PrimaryKey(autoGenerate = false)
    val userCreateTime: String,
    @SerializedName("createdAt")
    val serverCreateTime: String? = null

)