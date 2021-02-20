package com.mytelegram.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mytelegram.data.model.ConversationUser
import com.mytelegram.data.model.PersonMessage
import com.mytelegram.data.model.RoomMessage
import com.mytelegram.data.model.relationship.ConversationUserWithPersonMessage
import com.mytelegram.data.model.relationship.RoomWithMessagesAndUsers
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.data.network.UserApi
import com.mytelegram.data.repository.ChatRepository
import com.mytelegram.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
@HiltViewModel
class ChatViewModel @Inject constructor(private val chatRepository: ChatRepository) :
        BaseViewModel() {

    private var _roomWithMessagesAndUsers = MutableLiveData<RoomWithMessagesAndUsers>()
    val roomWithMessagesAndUsers: LiveData<RoomWithMessagesAndUsers> get() = _roomWithMessagesAndUsers

    fun getRoomWithMessagesAndUsers(roomId: String) = io {
        chatRepository.getRoomWithMessagesAndUsers(roomId).collect {
            _roomWithMessagesAndUsers.postValue(it)
        }
    }

    private var _conversationUserWithPersonMessage =
            MutableLiveData<ConversationUserWithPersonMessage>()
    val conversationUserWithPersonMessage: LiveData<ConversationUserWithPersonMessage> get() = _conversationUserWithPersonMessage
    fun getConversationUserWithPersonMessage(userId: String) = io {
        chatRepository.getConversationUserWithPersonMessage(userId).collect {
            _conversationUserWithPersonMessage.postValue(it)
        }
    }

    fun sendPersonMessage(personMessage: PersonMessage, cUser: ConversationUser) = io {

        when (val result = chatRepository.sendPersonMessageApi(personMessage, cUser)) {
            is Resource.Success -> {
                println(result.value)
            }
            is Resource.Failure -> {
                println(result.exception)
            }
            Resource.Loading -> Unit
        }
    }

    fun sendRoomMessage(roomMessage: RoomMessage) = io {

        when (val result = chatRepository.sendRoomMessageApi(roomMessage)) {
            is Resource.Success -> {
                println(result.value)
            }
            is Resource.Failure -> {
                println(result.exception)
            }
            Resource.Loading -> Unit
        }
    }

    fun deletePersonMessage(personMessage: PersonMessage) = io {
        if (!personMessage.messageId.isNullOrEmpty())
            when (val result = chatRepository.deletePersonMessageApi(personMessage.messageId)) {
                is Resource.Success -> {
                    println(result.value)
                }
                is Resource.Failure -> {
                    println(result.exception)
                }
                Resource.Loading -> Unit
            }
        else
            chatRepository.deletePersonMessageByUserCreateTimeDB(personMessage.userCreateTime)
    }

    fun deleteRoomMessage(roomMessage: RoomMessage) = io {
        if (!roomMessage.messageId.isNullOrEmpty())
            when (val result = chatRepository.deleteRoomMessageApi(roomMessage.messageId)) {
                is Resource.Success -> {
                    println(result.value)
                }
                is Resource.Failure -> {
                    println(result.exception)
                }
                Resource.Loading -> Unit
            }
        else
            chatRepository.deleteRoomMessageByUserCreateTimeDB(roomMessage.userCreateTime)
    }


    fun editRoomMessage(roomMessage: RoomMessage, newText: String): LiveData<Resource<Boolean>> {
        val result = MutableLiveData<Resource<Boolean>>()
        if (roomMessage.messageId == null)
            io {
                chatRepository.editRoomMessageDB(roomMessage.userCreateTime, newText)
                result.postValue(Resource.Success(true))
            }
        else
            io {
                result.postValue(chatRepository.editMessageApi(UserApi.EditMessageRequestType.ROOM_MESSAGE, roomMessage.messageId, newText))
            }
        return result
    }

    fun editPersonMessage(
            personMessage: PersonMessage,
            newText: String
    ): LiveData<Resource<Boolean>> {
        val result = MutableLiveData<Resource<Boolean>>()
        if (personMessage.messageId == null)
            io {
                chatRepository.editPersonMessageDB(personMessage.userCreateTime, newText)
                result.postValue(Resource.Success(true))
            }
        else
            io {
                result.postValue(
                        chatRepository.editMessageApi(
                                UserApi.EditMessageRequestType.PERSON_MESSAGE,
                                personMessage.messageId,
                                newText
                        )
                )
            }
        return result
    }
}