package com.mytelegram.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
data class ConversationUser(
        @SerializedName("_id")
        @PrimaryKey(autoGenerate = false)
        val userId: String,
        @SerializedName("user_name")
        val userName: String,
        @SerializedName("profile_url")
        val profileUrl: String?,
        @SerializedName("lastSeen")
        val lastSeen: String?
) : Serializable