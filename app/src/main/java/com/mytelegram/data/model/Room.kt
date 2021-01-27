package com.mytelegram.data.model


import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
data class Room(
    @SerializedName("_id")
    @PrimaryKey(autoGenerate = false)
    val roomId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar_url")
    val avatar_url: String? = null,
    @SerializedName("count_members")
    val count_members: Int
) : Serializable {

    @SerializedName("owner")
    @Ignore
    var owner: User? = null

    @SerializedName("admins")
    @Ignore
    var admins: List<User>? = null

    @SerializedName("members")
    @Ignore
    var members: List<User>? = null

    @SerializedName("messages")
    @Ignore
    var roomMessages: List<RoomMessage>? = null
}