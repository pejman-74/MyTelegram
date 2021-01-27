package com.mytelegram.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.room.withTransaction
import com.mytelegram.data.local.Database
import com.mytelegram.data.model.*
import com.mytelegram.data.model.relationship.ConversationUserWithPersonMessage
import com.mytelegram.data.model.relationship.RoomWithMessagesAndUsers
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.data.network.UserApi
import com.mytelegram.ui.home.HomeFeedItem
import com.mytelegram.ui.home.HomeRecyclerViewItemType
import com.mytelegram.util.*
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.Socket
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
        private val database: Database,
        private val userSocket: UserSocket,
        private val userApi: UserApi,
        @ApplicationContext private val context: Context
) : BaseRepository() {


    fun connectToUserServer(token: String): Socket? = userSocket.connect(token)
    fun disconnectFromUserServer() = userSocket.disconnect()

    suspend fun saveMainUser(mainUser: MainUser) {
        fetchImageUrl(mainUser.profileUrl)
        database.mainUserDao().insert(mainUser)
    }

    suspend fun userUpdateRequest() = safeApiCall { userApi.userUpdateRequest() }

    suspend fun logout(id: String) {
        database.mainUserDao().deleteById(id)
        safeApiCall { userApi.logOut() }
    }

    suspend fun search(text: String) =
            safeApiCall { userApi.search(text, UserApi.SearchRequestType.ROOM_AND_USER) }

    suspend fun searchUser(text: String) =
            safeApiCall { userApi.search(text, UserApi.SearchRequestType.USER) }


    private suspend fun getImageFromServer(
            imageId: String,
            sizeRequestType: UserApi.ImageSizeRequestType
    ) = safeApiCall { userApi.requestImage(imageId, sizeRequestType) }


    fun getMainUser() = database.mainUserDao().getMainUser()

    private fun getRoomWithMessages() =
            database.roomDao().getRoomWithMessages().filterNotNull()


    suspend fun getRoomSampleMembers(roomId: String) =
            safeApiCall { userApi.getRoomSampleMembers(roomId) }


    fun homeFeedItemAdapter() =
            getConversationUsersWithPersonMessages().combine(getRoomWithMessages())
            { listConversationUserWithPersonMessage: List<ConversationUserWithPersonMessage>,
              listRoomWithUsersAndMessageAndUsers: List<RoomWithMessagesAndUsers> ->

                val homeFeedItemList = ArrayList<HomeFeedItem>()

                listRoomWithUsersAndMessageAndUsers.forEach { roomWithUsersAndMessages ->
                    //get user from database
                    homeFeedItemList.add(
                            HomeFeedItem(
                                    roomWithUsersAndMessages.room.roomId,
                                    HomeRecyclerViewItemType.RoomItemHome(roomWithUsersAndMessages.room),
                                    roomWithUsersAndMessages.roomMessages.lastOrNull()?.text
                            )
                    )
                }

                listConversationUserWithPersonMessage.forEach { userWithPersonMessage ->
                    homeFeedItemList.add(
                            HomeFeedItem(
                                    userWithPersonMessage.conversationUser.userId,
                                    HomeRecyclerViewItemType.ConversationUserItemHome(userWithPersonMessage.conversationUser),
                                    userWithPersonMessage.sentPersonMessages.plus(userWithPersonMessage.receivedPersonMessages)
                                            .maxByOrNull { it.userCreateTime }?.text
                            )
                    )
                }

                return@combine homeFeedItemList
            }

    private suspend fun fetchImageUrl(imageId: String?) {
        val imageFile = File(context.getPictureDir(), "$imageId.jpg")
        if (!imageId.isNullOrBlank() && !imageFile.exists()) {
            when (val result = getImageFromServer(imageId, UserApi.ImageSizeRequestType.SMALL)) {
                is Resource.Success -> {
                    File(context.getPictureDir(), "${imageId}.jpg")
                            .writeBitmap(result.value, Bitmap.CompressFormat.JPEG, 100)
                }
                is Resource.Failure -> {
                    println("Fetch image is failed")
                }
            }
        }
    }

    private fun deleteImageFile(imageId: String?) {
        val imageFile = File(context.getPictureDir(), "$imageId.jpg")
        if (imageFile.exists())
            imageFile.delete()
    }

    suspend fun insertOrUpdateRoom(room: Room) {
        fetchImageUrl(room.avatar_url)
        database.roomDao().insert(room)

        room.roomMessages?.let { database.roomMessageDao().insert(it) }

        room.owner?.let {
            fetchImageUrl(it.profileUrl)
            database.roomUserDao().insert(it.toRoomUser(room.roomId, "owner"))
        }
        room.admins?.let { roomUserList ->
            database.roomUserDao().insert(roomUserList.map {
                fetchImageUrl(it.profileUrl)
                it.toRoomUser(room.roomId, "admin")
            })
        }
        room.members?.let { roomUserList ->
            database.roomUserDao().insert(roomUserList.map {
                fetchImageUrl(it.profileUrl)
                it.toRoomUser(room.roomId, "member")
            })
        }
    }

    suspend fun deleteRoom(roomId: String) {
        deleteImageFile(getRoomById(roomId)?.avatar_url)
        database.roomDao().deleteById(roomId)
        database.roomMessageDao().deleteByRoomId(roomId)
        database.roomUserDao().deleteByRoomId(roomId)
    }

    /*
    * when server emit events they are in order.
    * so must do event crud's in transaction to prevent conflicts
    * used this in HomeViewModel.
    * */
    suspend fun safeTransaction(block: suspend () -> Unit) =
            database.withTransaction { block.invoke() }


    suspend fun getRoomById(id: String) = database.roomDao().getRoomById(id)

    suspend fun deleteRoomApi(roomId: String): Resource<Boolean> {
        val apiResult = safeApiCall { userApi.deleteRoom(roomId) }
        when (apiResult) {
            is Resource.Success -> {
                deleteRoom(roomId)
            }
            else -> {
                println("room delete is failed")
            }
        }
        return apiResult
    }

    suspend fun insertOrUpdateRoomMessage(roomMessage: RoomMessage) =
            database.roomMessageDao().insert(roomMessage)

    suspend fun deleteRoomMessage(roomMessageId: String) =
            database.roomMessageDao().deleteById(roomMessageId)

    suspend fun insertOrUpdatePersonMessage(personMessage: PersonMessage) =
            database.personMessageDao().insert(personMessage)

    suspend fun deletePersonMessage(personMessageId: String) =
            database.personMessageDao().deleteByMessageId(personMessageId)

    suspend fun insertOrUpdateConversationUser(conversationUser: ConversationUser) {
        fetchImageUrl(conversationUser.profileUrl)
        database.conversationUserDao().insert(conversationUser)
    }

    suspend fun insertOrUpdateRoomUser(roomId: String, user: User, role: String) =
            database.roomUserDao().insert(user.toRoomUser(roomId, role))


    suspend fun deleteConversationUserApi(conversationUserId: String) {
        userApi.deleteConversionUser(conversationUserId)
        deleteConversationUser(conversationUserId)
    }

    suspend fun deleteConversationUser(conversationUserId: String) {
        database.conversationUserDao().getConversationUser(conversationUserId)?.let {
            deleteImageFile(it.profileUrl)
        }
        database.conversationUserDao().deleteConversationUserById(conversationUserId)
        database.personMessageDao().deleteByMessageOwnerOrReceiverUser(conversationUserId)
    }

    suspend fun deleteRoomUser(roomId: String, roomUserId: String, role: String) {
        database.roomUserDao().deleteById(roomUserId + roomId)
    }

    suspend fun setConversationUserOnOffDB(conversationUserId: String, status: String) =
            database.conversationUserDao().setOnOfStatus(conversationUserId, status)

    private fun getConversationUsersWithPersonMessages() =
            database.conversationUserDao().getConversationUsersWithPersonMessages().filterNotNull()

    suspend fun joinToRoom(roomId: String): Resource<Boolean> =
            when (val apiResult = safeApiCall { userApi.joinToRoom(roomId) }) {
                is Resource.Success -> {
                    insertOrUpdateRoom(apiResult.value)
                    Resource.Success(true)
                }
                is Resource.Failure -> {
                    Resource.Failure(apiResult.exception)
                }
                Resource.Loading -> {
                    Resource.Loading
                }
            }


    suspend fun createRoom(name: String, avatar_url: String, members: Array<String>) =
            safeApiCall { userApi.createRoom(name, avatar_url, members) }
}


