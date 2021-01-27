package com.mytelegram.data.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mytelegram.data.model.MainUser
import kotlinx.coroutines.flow.Flow

@Dao
interface MainUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mainUser: MainUser)

    @Query("delete from MainUser where id=:id")
    suspend fun deleteById(id:String)

    @Query("Select * from MainUser")
    fun getMainUser(): Flow<MainUser?>

}