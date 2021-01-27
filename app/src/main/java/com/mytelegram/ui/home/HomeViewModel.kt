package com.mytelegram.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.mytelegram.data.model.*
import com.mytelegram.data.model.resouces.ConnectionStatus
import com.mytelegram.data.model.resouces.Event
import com.mytelegram.data.model.resouces.EventConnectionStatus
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.data.model.response.SearchResponse
import com.mytelegram.data.repository.HomeRepository
import com.mytelegram.ui.base.BaseViewModel
import com.mytelegram.util.mainUser
import com.mytelegram.util.stringToJsonObject
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map

class HomeViewModel @ViewModelInject constructor(
        private val homeRepository: HomeRepository
) : BaseViewModel() {


    val homeFeed = homeRepository.homeFeedItemAdapter().asLiveData(Dispatchers.IO)
    val getLiveMainUser by lazy {
        homeRepository.getMainUser().map {

            //save mainUser in global var to use in other places
            it?.let {
                mainUser = it
            }
            //wrap to event
            Event(it)
        }.asLiveData(Dispatchers.IO)
    }


    suspend fun getRoom(roomId: String) = homeRepository.getRoomById(roomId)

    suspend fun getRoomSampleMembers(roomId: String) = homeRepository.getRoomSampleMembers(roomId)

    fun search(text: String): LiveData<Resource<SearchResponse>> {
        val searchResource = MutableLiveData<Resource<SearchResponse>>()
        io {
            searchResource.postValue(homeRepository.search(text))
        }
        return searchResource
    }

    fun searchUser(text: String): LiveData<Resource<SearchResponse>> {
        val searchResource = MutableLiveData<Resource<SearchResponse>>()
        io {
            searchResource.postValue(homeRepository.searchUser(text))
        }
        return searchResource
    }

    fun createRoom(name: String, avatar_url: String, members: Array<String>):
            LiveData<Resource<Boolean>> {
        val createRoomResource = MutableLiveData<Resource<Boolean>>()
        createRoomResource.postValue(Resource.Loading)
        io {
            createRoomResource.postValue(homeRepository.createRoom(name, avatar_url, members))
        }
        return createRoomResource
    }

    suspend fun joinToRoom(roomId: String) = homeRepository.joinToRoom(roomId)

    fun deleteConversionUser(id: String) = io {
        homeRepository.deleteConversationUserApi(id)
    }

    fun deleteRoom(id: String) = io {
        homeRepository.deleteRoomApi(id)
    }

    fun logout() = io { homeRepository.logout(mainUser.id) }

    private fun safeTransactionCall(transaction: suspend () -> Unit) = io {
        homeRepository.safeTransaction { transaction.invoke() }
    }

    fun disconnectFromUserServer() = homeRepository.disconnectFromUserServer()

    private val _connectionStatus =
            MutableLiveData(EventConnectionStatus(ConnectionStatus.Disconnect))
    val connectionStatus: LiveData<EventConnectionStatus> get() = _connectionStatus

    /*
      connecting to chat server and listen to events until app available in Stack.
      as soon as the user is connected user get last feed changes.
    */

    fun connectToUserServer(token: String) {
        homeRepository.connectToUserServer(token)?.also { socket ->
            socket.on(Socket.EVENT_CONNECT) {
                _connectionStatus.postValue(EventConnectionStatus(ConnectionStatus.Connect))
                io {
                    homeRepository.userUpdateRequest()
                }
            }
            socket.on(Socket.EVENT_DISCONNECT) {
                _connectionStatus.postValue(EventConnectionStatus(ConnectionStatus.Disconnect))
            }
            socket.on(Socket.EVENT_ERROR) { args ->
                if (args[0] != null && args[0].toString().startsWith("ae:"))
                    _connectionStatus.postValue(EventConnectionStatus(ConnectionStatus.Error(true)))
                else
                    _connectionStatus.postValue(EventConnectionStatus(ConnectionStatus.Error(false)))
            }
            socket.on("mainUserInsertOrUpdate") { dataArray ->
                val roomMessage =
                        stringToJsonObject(dataArray[0].toString(), MainUser::class.java)
                safeTransactionCall { homeRepository.saveMainUser(roomMessage) }
            }
            socket.on("roomMessageInsertOrUpdate") { dataArray ->
                val roomMessage =
                        stringToJsonObject(dataArray[0].toString(), RoomMessage::class.java)
                safeTransactionCall {
                    homeRepository.insertOrUpdateRoomMessage(roomMessage)
                }
            }
            socket.on("roomMessageDelete") { dataArray ->
                val roomMessageId = dataArray[0].toString()
                safeTransactionCall {
                    homeRepository.deleteRoomMessage(roomMessageId)
                }
            }
            socket.on("roomInsertOrUpdate") { dataArray ->
                println("roomInsertOrUpdate")
                println(dataArray[0].toString())
                val room = stringToJsonObject(dataArray[0].toString(), Room::class.java)
                safeTransactionCall { homeRepository.insertOrUpdateRoom(room) }
            }
            socket.on("roomDelete") { dataArray ->
                println("roomDelete")
                val roomId = dataArray[0].toString()
                safeTransactionCall { homeRepository.deleteRoom(roomId) }
            }
            socket.on("personMessageInsertOrUpdate") { dataArray ->
                println("personMessageInsertOrUpdate")
                val personMessage =
                        stringToJsonObject(dataArray[0].toString(), PersonMessage::class.java)
                safeTransactionCall { homeRepository.insertOrUpdatePersonMessage(personMessage) }
            }

            socket.on("personMessageDelete") { dataArray ->
                println("personMessageDelete")
                val personMessageId = dataArray[0].toString()
                safeTransactionCall {
                    homeRepository.deletePersonMessage(personMessageId)
                }
            }

            socket.on("conversationUserInsertOrUpdate") { dataArray ->
                println("conversationUserInsertOrUpdate")
                val conversationUser =
                        stringToJsonObject(dataArray[0].toString(), ConversationUser::class.java)
                safeTransactionCall {
                    homeRepository.insertOrUpdateConversationUser(conversationUser)
                }
            }

            socket.on("conversationUserDelete") { dataArray ->
                println("conversationUserDelete")
                val conversationUserId = dataArray[0].toString()
                safeTransactionCall { homeRepository.deleteConversationUser(conversationUserId) }
            }

            socket.on("roomUserInsertOrUpdate") { dataArray ->
                println("roomUserInsertOrUpdate")
                val roomId = dataArray[0].toString()
                val user =
                        stringToJsonObject(dataArray[1].toString(), User::class.java)
                val userRole = dataArray[2].toString()
                safeTransactionCall {
                    homeRepository.insertOrUpdateRoomUser(roomId, user, userRole)
                }
            }

            socket.on("roomUserDelete") { dataArray ->
                println("roomUserDelete")
                val roomId = dataArray[0].toString()
                val roomUserId = dataArray[1].toString()
                val userRole = dataArray[2].toString()
                safeTransactionCall { homeRepository.deleteRoomUser(roomId, roomUserId, userRole) }
            }


            socket.on("userOffOn") { dataArray ->
                println("userOffOn")
                val conversationUserId = dataArray[0].toString()
                //If the status is not equal to "On",server sends "lastSeen"
                val status = dataArray[1].toString()
                safeTransactionCall {
                    homeRepository.setConversationUserOnOffDB(
                            conversationUserId,
                            status
                    )
                }
            }

        }

    }


}
