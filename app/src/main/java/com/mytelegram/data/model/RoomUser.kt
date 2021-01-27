package com.mytelegram.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class RoomUser(
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        val id: String,
        @SerializedName("userId")
        val userId: String,
        @SerializedName("userName")
        val userName: String,
        @SerializedName("profileUrl")
        val profileUrl: String?,
        @SerializedName("lastSeen")
        val lastSeen: String?,
        @SerializedName("roomOwner")
        val roomOwner: String,
        @SerializedName("role")
        val role: String
)
