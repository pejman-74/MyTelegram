package com.mytelegram.data.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mytelegram.data.model.RoomMessage

@Dao
interface RoomMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(roomMessage: RoomMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(roomMessages: List<RoomMessage>)

    @Query("delete from RoomMessage where roomOwner=:roomId")
    suspend fun deleteByRoomId(roomId: String)

    @Query("delete from RoomMessage where messageId=:id")
    suspend fun deleteById(id: String)

    @Query("delete from RoomMessage where userCreateTime=:userCreateTime")
    suspend fun deleteByUserCreateTime(userCreateTime: String)

    @Query("update RoomMessage set text=:newText where userCreateTime=:userCreateTime")
    suspend fun updateMessageText(userCreateTime: String, newText: String)

}