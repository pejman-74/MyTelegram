package com.mytelegram.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class MainUser(
        @SerializedName("_id")
        @PrimaryKey(autoGenerate = false)
        val id: String,
        @SerializedName("phone_number")
        val phoneNumber: String,
        @SerializedName("user_name")
        val userName: String,
        @SerializedName("profile_url")
        val profileUrl: String?,
        @SerializedName("last_auth_token")
        val lastAuthToken: String
)