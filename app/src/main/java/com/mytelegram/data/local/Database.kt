package com.mytelegram.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mytelegram.data.model.*
import com.mytelegram.data.model.dao.*

/*
* DESCRIPTIONS OF APP DATABASE STRUCTURE.
*
* There is three type User:
*   1-MainUser: Current logged user into app.(just one user)
*   2-ConversionUser: Uses for p2p conversions with main user.
*   2-RoomUser: Uses for save room(groups) members such as owner,admins,users.
*
* There is tow type Message:
*   1-personMessage: Uses for p2p conversions
*   2-RoomMessage:Uses for Rooms messages
*
* Room :
*   Room uses for save user groups.(membership or owner)
*
* RELATIONSHIP BETWEEN ENTITY'S
*   ConversationUser With PersonMessage : 1:N Relation type. uses for get user with messages collectively.
*   Room With Messages And Users : 1:N Relation type. uses for get room with messages and members collectively.
* */
@Database(
    entities = [RoomUser::class, RoomMessage::class, Room::class, MainUser::class,
        PersonMessage::class, ConversationUser::class], version = 1
)
abstract class Database : RoomDatabase() {
    abstract fun mainUserDao(): MainUserDao
    abstract fun roomDao(): RoomDao
    abstract fun roomUserDao(): RoomUserDao
    abstract fun conversationUserDao(): ConversationUserDao
    abstract fun roomMessageDao(): RoomMessageDao
    abstract fun personMessageDao(): PersonMessageDao
}