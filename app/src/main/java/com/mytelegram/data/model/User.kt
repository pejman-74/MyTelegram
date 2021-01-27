package com.mytelegram.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class User(
        @SerializedName("_id")
        val userId: String,
        @SerializedName("user_name")
        val userName: String,
        @SerializedName("profile_url")
        val profileUrl: String?,
        @SerializedName("lastSeen")
        val lastSeen: String?
) : Parcelable
