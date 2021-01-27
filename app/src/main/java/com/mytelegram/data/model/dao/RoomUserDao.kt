package com.mytelegram.data.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mytelegram.data.model.RoomUser

@Dao
interface RoomUserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: RoomUser)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(users: List<RoomUser>)

    @Query("Delete from RoomUser where id=:id")
    suspend fun deleteById(id: String)

    @Query("Delete from RoomUser where roomOwner=:id")
    suspend fun deleteByRoomId(id: String)

}