package com.mytelegram.data.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mytelegram.data.model.ConversationUser
import com.mytelegram.data.model.relationship.ConversationUserWithPersonMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversationUser: ConversationUser)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun ignoreInsert(conversationUser: ConversationUser)

    @Query("delete from ConversationUser where userId=:id")
    suspend fun deleteConversationUserById(id: String)

    //set user online/offline statues.
    @Query("Update  ConversationUser set lastSeen=:status where userId=:id")
    suspend fun setOnOfStatus(id: String, status: String)

    @Query("Select * from ConversationUser where userId=:id")
    suspend fun getConversationUser(id: String): ConversationUser?

    @Query("Select * from ConversationUser")
    fun getConversationUsersWithPersonMessages(): Flow<List<ConversationUserWithPersonMessage>?>

    @Query("Select * from ConversationUser where userId=:userId ")
    fun getConversationUsersWithPersonMessages(userId: String): Flow<ConversationUserWithPersonMessage?>
}