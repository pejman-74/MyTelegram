package com.mytelegram.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.mytelegram.data.model.PersonMessage
import com.mytelegram.data.model.Room
import com.mytelegram.data.model.RoomMessage
import com.mytelegram.data.model.response.SearchResponse
import com.mytelegram.util.UserSocket
import io.socket.client.Ack
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
/*
* After user login uses this apis for all user actions
* */
class UserApi(private val userSocket: UserSocket) : BaseApi() {
    init {
        userSocket.getSocket()?.connect()
    }

    //image size tell to server image quality
    enum class ImageSizeRequestType(val size: String) {
        SMALL("small"),
        LARGE("large")
    }

    //search type to tell server require objects
    enum class SearchRequestType(val type: String) {
        USER("user"),
        ROOM("room"),
        ROOM_AND_USER("roomAndUser")
    }

    //edit message type: r=> room message  u=>person message
    enum class EditMessageRequestType(val type: String) {
        PERSON_MESSAGE("p"),
        ROOM_MESSAGE("r"),
    }
    suspend fun userUpdateRequest(): Boolean = suspendCoroutine { cont ->

        userSocket.getSocket()?.emit(
            "userUpdateRequestDetection",
            Ack { ackData ->
                try {
                    cont.resume(safeApiResultConverter(ackData))
                } catch (err: Throwable) {
                    cont.resumeWithException(err)
                }
            })
    }

    //request profile image request
    suspend fun requestImage(imageId: String, sizeRequestType: ImageSizeRequestType): Bitmap =
        suspendCoroutine { cont ->

            userSocket.getSocket()?.emit(
                "imageRequest",
                imageId,
                sizeRequestType.size,
                Ack { ackData ->
                    if (ackData[0] != null && ackData[0] != "failed") {
                        val imageBytes = Base64.decode(ackData[0].toString(), 0)
                        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        cont.resume(image)
                    } else
                        cont.resumeWithException(Exception("Image requesting failed"))
                })
        }

    suspend fun deleteConversionUser(conversationUserId: String): Boolean =
        suspendCoroutine { cont ->
            userSocket.getSocket()!!.emit(
                "deleteConversationUserDetection",
                conversationUserId,
                Ack { ackData ->
                    try {
                        cont.resume(safeApiResultConverter(ackData))
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }

    suspend fun editMessage(editMessageRequestType: EditMessageRequestType, messageId: String, newText: String): Boolean =
        suspendCoroutine { cont ->
            userSocket.getSocket()?.emit("editMessageDetection", editMessageRequestType.type, messageId, newText,
                Ack { ackData ->
                    try {
                        cont.resume(safeApiResultConverter(ackData))
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }

    suspend fun sendPersonMessage(personMessage: PersonMessage): Boolean =
        suspendCoroutine { cont ->
            userSocket.getSocket()?.emit(
                "newPersonMessageDetection",
                personMessage.receiverUser,
                personMessage.text,
                personMessage.userCreateTime,
                Ack { ackData ->
                    try {
                        cont.resume(safeApiResultConverter(ackData))
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }

    suspend fun deletePersonMessage(personMessageId: String): Boolean = suspendCoroutine { cont ->
        userSocket.getSocket()?.emit(
            "deletePersonMessageDetection",
            personMessageId,
            Ack { ackData ->
                try {
                    cont.resume(safeApiResultConverter(ackData))
                } catch (err: Throwable) {
                    cont.resumeWithException(err)
                }
            })
    }

    suspend fun sendRoomMessage(roomMessage: RoomMessage): Boolean = suspendCoroutine { cont ->
        userSocket.getSocket()?.emit(
            "newRoomMessageDetection",
            roomMessage.roomOwner,
            roomMessage.text,
            roomMessage.userCreateTime,
            Ack { ackData ->
                try {
                    cont.resume(safeApiResultConverter(ackData))
                } catch (err: Throwable) {
                    cont.resumeWithException(err)
                }
            })
    }

    suspend fun deleteRoomMessage(roomMessageId: String): Boolean = suspendCoroutine { cont ->
        userSocket.getSocket()?.emit(
            "deleteRoomMessageDetection",
            roomMessageId,
            Ack { ackData ->
                try {
                    cont.resume(safeApiResultConverter(ackData))
                } catch (err: Throwable) {
                    cont.resumeWithException(err)
                }
            })
    }

    suspend fun createRoom(name: String, avatar_url: String, members: Array<String>): Boolean =
        suspendCoroutine { cont ->
            userSocket.getSocket()?.emit(
                "newRoomDetection",
                name, avatar_url, members.joinToString(prefix = "[", postfix = "]") { "\"$it\"" },
                Ack { ackData ->
                    try {
                        cont.resume(safeApiResultConverter(ackData))
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }

    suspend fun joinToRoom(roomId: String): Room =
        suspendCoroutine { cont ->
            userSocket.getSocket()?.emit(
                "joinToRoomDetection",
                roomId,
                Ack { ackData ->
                    try {
                        val response = safeApiResultConverter(ackData, Room::class.java)
                        cont.resume(response)
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }

    suspend fun deleteRoom(roomId: String): Boolean =
        suspendCoroutine { cont ->
            userSocket.getSocket()?.emit(
                "deleteRoomDetection",
                roomId,
                Ack { ackData ->
                    try {
                        cont.resume(safeApiResultConverter(ackData))
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }


    suspend fun getRoomSampleMembers(roomId: String): Room =
        suspendCoroutine { cont ->
            userSocket.getSocket()?.emit(
                "getRoomSampleMembersDetection",
                roomId,
                Ack { ackData ->
                    try {
                        val response = safeApiResultConverter(ackData, Room::class.java)
                        cont.resume(response)
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }

    suspend fun logOut(): Boolean =
        suspendCoroutine { cont ->
            userSocket.getSocket()?.emit(
                "logOutDetection",
                Ack { ackData ->
                    try {
                        cont.resume(safeApiResultConverter(ackData))
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }


    suspend fun search(text: String, searchRequestType: SearchRequestType): SearchResponse =
        suspendCoroutine { cont ->
            userSocket.getSocket()?.emit(
                "searchDetection",
                text,
                searchRequestType.type,
                Ack { ackData ->
                    try {
                        val response = safeApiResultConverter(ackData, SearchResponse::class.java)
                        cont.resume(response)
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }
}
