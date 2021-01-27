package com.mytelegram.data.model.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.mytelegram.data.model.Room
import com.mytelegram.data.model.RoomMessage
import com.mytelegram.data.model.RoomUser

data class RoomWithMessagesAndUsers(
        @Embedded val room: Room,
        @Relation(parentColumn = "roomId", entityColumn = "roomOwner")
        val roomMessages: MutableList<RoomMessage>,
        @Relation(parentColumn = "roomId", entityColumn = "roomOwner")
        val roomUsers: MutableList<RoomUser>
)