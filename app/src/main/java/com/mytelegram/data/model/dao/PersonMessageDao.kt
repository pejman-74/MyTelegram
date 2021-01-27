package com.mytelegram.data.model.dao

import androidx.room.*
import com.mytelegram.data.model.PersonMessage

@Dao
interface PersonMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(personMessage: PersonMessage)

    @Query("delete from PersonMessage where messageId=:messageId")
    suspend fun deleteByMessageId(messageId: String)

    @Query("delete from PersonMessage where userCreateTime=:userCreateTime")
    suspend fun deleteByUserCreateTime(userCreateTime: String)

    @Transaction
    @Query("delete from PersonMessage where messageOwner=:id or receiverUser=:id")
    suspend fun deleteByMessageOwnerOrReceiverUser(id: String)

    @Query("update PersonMessage set text=:newText where userCreateTime=:userCreateTime")
    suspend fun updateMessageText(userCreateTime: String, newText: String)

}