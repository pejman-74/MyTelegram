package com.mytelegram.data.model.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.mytelegram.data.model.ConversationUser
import com.mytelegram.data.model.PersonMessage

data class ConversationUserWithPersonMessage(
   @Embedded val conversationUser: ConversationUser,

   @Relation(parentColumn = "userId", entityColumn = "messageOwner")
   val receivedPersonMessages: List<PersonMessage>,

   @Relation(parentColumn = "userId", entityColumn = "receiverUser")
   val sentPersonMessages: List<PersonMessage>
)