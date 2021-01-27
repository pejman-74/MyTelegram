package com.mytelegram.data.repository

import com.mytelegram.data.local.Database
import com.mytelegram.data.model.ConversationUser
import com.mytelegram.data.model.PersonMessage
import com.mytelegram.data.model.RoomMessage
import com.mytelegram.data.model.relationship.ConversationUserWithPersonMessage
import com.mytelegram.data.model.relationship.RoomWithMessagesAndUsers
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.data.network.UserApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject


class ChatRepository @Inject constructor(
    private val userApi: UserApi,
    private val database: Database,
) : BaseRepository() {


    suspend fun sendPersonMessageApi(
        personMessage: PersonMessage,
        cUser: ConversationUser
    ): Resource<Boolean> {
        database.conversationUserDao().ignoreInsert(cUser)
        database.personMessageDao().insert(personMessage)
        return safeApiCall { userApi.sendPersonMessage(personMessage) }
    }

    suspend fun deletePersonMessageApi(personMessageId: String): Resource<Boolean> {
        database.personMessageDao().deleteByMessageId(personMessageId)
        return safeApiCall { userApi.deletePersonMessage(personMessageId) }
    }

    suspend fun deletePersonMessageByUserCreateTimeDB(userCreateTime: String) =
        database.personMessageDao().deleteByUserCreateTime(userCreateTime)

    suspend fun editMessageApi(type: UserApi.EditMessageRequestType, messageId: String, newText: String) =
        safeApiCall { userApi.editMessage(type, messageId, newText) }

    suspend fun editRoomMessageDB(userCreateTime: String, newText: String) =
        database.roomMessageDao().updateMessageText(userCreateTime, newText)

    suspend fun editPersonMessageDB(userCreateTime: String, newText: String) =
        database.personMessageDao().updateMessageText(userCreateTime, newText)

    suspend fun deleteRoomMessageApi(roomMessageId: String): Resource<Boolean> {
        database.roomMessageDao().deleteById(roomMessageId)
        return safeApiCall { userApi.deleteRoomMessage(roomMessageId) }
    }

    suspend fun deleteRoomMessageByUserCreateTimeDB(userCreateTime: String) =
        database.roomMessageDao().deleteByUserCreateTime(userCreateTime)


    suspend fun sendRoomMessageApi(roomMessage: RoomMessage): Resource<Boolean> {
        database.roomMessageDao().insert(roomMessage)
        return safeApiCall { userApi.sendRoomMessage(roomMessage) }
    }

    fun getRoomWithMessagesAndUsers(roomId: String): Flow<RoomWithMessagesAndUsers> =
        database.roomDao().getRoomWithMessagesAndUsers(roomId).filterNotNull()

    fun getConversationUserWithPersonMessage(userId: String): Flow<ConversationUserWithPersonMessage> =
        database.conversationUserDao().getConversationUsersWithPersonMessages(userId)
            .filterNotNull()

}
