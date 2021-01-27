package com.mytelegram.ui.chat

import android.graphics.Point
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mytelegram.R
import com.mytelegram.data.model.RoomMessage
import com.mytelegram.data.model.RoomUser
import com.mytelegram.databinding.ReceivedMessageBinding
import com.mytelegram.databinding.SentMessageBinding
import com.mytelegram.util.*
import com.mytelegram.util.custom_view.AvatarImageView
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


interface RoomMessageItemListener {
    fun onMessageItemClick(roomMessage: RoomMessage, view: View, point: Point)
}

class RoomMessageItemAdapter(
        private val itemEventListener: RoomMessageItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //used AsyncListDiffer for improve recycler view performance
    private val messageDiffUtilItemCallback = object :
            DiffUtil.ItemCallback<GroupRecyclerViewItems>() {
        override fun areItemsTheSame(
                oldItem: GroupRecyclerViewItems, newItem: GroupRecyclerViewItems
        ) = when (oldItem) {
            is GroupRecyclerViewItems.MessageItem ->
                oldItem.roomMessageItem.userCreateTime == (newItem as GroupRecyclerViewItems.MessageItem).roomMessageItem.userCreateTime
        }

        override fun areContentsTheSame(
                oldItem: GroupRecyclerViewItems,
                newItem: GroupRecyclerViewItems
        ) = when (oldItem) {
            is GroupRecyclerViewItems.MessageItem ->
                oldItem.roomMessageItem == (newItem as GroupRecyclerViewItems.MessageItem).roomMessageItem
        }
    }


    private val differ = AsyncListDiffer(this, messageDiffUtilItemCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.received_message -> {
                val binding = ReceivedMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
                ReceivedMessageViewHolder(binding)
            }
            R.layout.sent_message -> {
                val binding =
                        SentMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SentMessageViewHolder(binding)
            }
            else -> {
                throw IllegalFormatFlagsException("Can't find appropriate viewHolder!")
            }
        }


    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = differ.currentList[position]) {
            is GroupRecyclerViewItems.MessageItem -> {
                if (item.roomMessageItem.messageOwner == mainUser.id)
                    R.layout.sent_message
                else
                    R.layout.received_message
            }
            else -> {
                TODO()
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]

        when (holder) {
            is ReceivedMessageViewHolder -> {
                val message = item as GroupRecyclerViewItems.MessageItem
                holder.bind(message.roomMessageItem)
            }
            is SentMessageViewHolder -> {
                val message = item as GroupRecyclerViewItems.MessageItem
                holder.bind(message.roomMessageItem)
            }
        }


    }

    fun setMessageData(newMessages: List<GroupRecyclerViewItems>) {
        differ.submitList(newMessages)
    }

    //this list store last update room members users
    private var roomUserList = ArrayList<RoomUser>()

    //handel group members user changes then apply changes with message location in adapter
    fun setUserData(newRoomUserList: List<RoomUser>) {

        val sum = newRoomUserList + roomUserList
        val updatedRoomUsers = sum.groupBy { it.id }
                .filter { it.value.size == 1 || it.value[0] != it.value[1] }
                .flatMap { it.value }
        roomUserList = ArrayList(newRoomUserList)
        updatedRoomUsers.forEach { roomUser ->
            differ.currentList.forEachIndexed { index, groupRecyclerViewItems ->
                when (groupRecyclerViewItems) {
                    is GroupRecyclerViewItems.MessageItem ->
                        if (groupRecyclerViewItems.roomMessageItem.messageOwner == roomUser.userId)
                            notifyItemChanged(index)

                }
            }
        }

    }

    override fun getItemCount() = differ.currentList.size


    inner class ReceivedMessageViewHolder(private val itemBinding: ReceivedMessageBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(roomMessage: RoomMessage) {
            itemBinding.tvChatMessage.text = roomMessage.text
            itemBinding.tvChatTime.text = roomMessage.userCreateTime.toLocalTime()
            //in received message show tail in left
            itemBinding.cvChatMessage.setTailLength("left", 10f)

            //detect sender avatar
            itemBinding.receivedMessageAivAvatar.apply {
                roomUserList.find { it.userId == roomMessage.messageOwner }?.let { roomUser ->
                    if (roomUser.profileUrl.isNullOrBlank())
                        text = getAvatarText(roomUser.userName)
                    else {
                        setShowState(AvatarImageView.SHOW_IMAGE)
                        File(context.getPictureDir(), "${roomUser.profileUrl}.jpg").apply {
                            if (exists())
                                load(this)
                        }

                    }
                }


            }
            itemView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        itemEventListener.onMessageItemClick(roomMessage, v, Point(event.x.toInt(), event.y.toInt()))
                        v.performClick()
                    }
                }
                true
            }
        }
    }

    inner class SentMessageViewHolder(var itemBinding: SentMessageBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(roomMessage: RoomMessage) {
            itemBinding.tvChatMessage.text = roomMessage.text
            //in received message show tail in right
            itemBinding.cvChatMessage.setTailLength("right", 10f)

            //detect if message sent show tick or show watch animation
            itemBinding.lavMessageStatus.apply {
                if (roomMessage.messageId.isNullOrEmpty()) {
                    itemBinding.tvChatTime.text = roomMessage.userCreateTime.toLocalTime()
                    setAnimation(R.raw.message_watch)
                } else {
                    itemBinding.tvChatTime.text = roomMessage.serverCreateTime!!.toLocalTime()
                    load(R.drawable.ic_check)
                }
            }
            //sent to fragment which item clicked and touch location
            itemView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        itemEventListener.onMessageItemClick(roomMessage, v, Point(event.x.toInt(), event.y.toInt()))
                        v.performClick()
                    }
                }
                true
            }

        }
    }

}

/*
* currently group chat recycler view have message item but in future simply can
* add new element
* */
sealed class GroupRecyclerViewItems {
    class MessageItem(val roomMessageItem: RoomMessage) : GroupRecyclerViewItems()
}
