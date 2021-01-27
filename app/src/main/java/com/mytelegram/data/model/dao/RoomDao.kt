package com.mytelegram.data.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mytelegram.data.model.Room
import com.mytelegram.data.model.relationship.RoomWithMessagesAndUsers
import kotlinx.coroutines.flow.Flow


@Dao
interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: Room)

    @Query("select * from room where roomId=:roomId")
    suspend fun getRoomById(roomId: String): Room?


    @Query("delete from room where roomId=:roomId")
    suspend fun deleteById(roomId: String)


    @Query("select * from Room where roomId=:roomId ")
    fun getRoomWithMessagesAndUsers(roomId: String): Flow<RoomWithMessagesAndUsers?>


    @Query("select * from Room ")
    fun getRoomWithMessages(): Flow<List<RoomWithMessagesAndUsers>?>

}