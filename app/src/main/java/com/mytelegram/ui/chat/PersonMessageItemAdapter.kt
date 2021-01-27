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
import com.mytelegram.data.model.PersonMessage
import com.mytelegram.databinding.ReceivedMessageBinding
import com.mytelegram.databinding.SentMessageBinding
import com.mytelegram.util.mainUser
import com.mytelegram.util.setTailLength
import com.mytelegram.util.toLocalTime
import java.util.*


interface PersonMessageItemListener {
    fun onMessageItemClick(personMessage: PersonMessage, view: View, point: Point)
}

class PersonMessageItemAdapter(
        private val itemEventListener: PersonMessageItemListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //used AsyncListDiffer for improve recycler view performance
    private val messageDiffUtilItemCallback = object :
            DiffUtil.ItemCallback<PersonRecyclerViewItems>() {
        override fun areItemsTheSame(
                oldItem: PersonRecyclerViewItems, newItem: PersonRecyclerViewItems
        ) = when (oldItem) {
            is PersonRecyclerViewItems.MessageItem ->
                oldItem.personMessageItem.userCreateTime ==
                        (newItem as PersonRecyclerViewItems.MessageItem).personMessageItem.userCreateTime
        }

        override fun areContentsTheSame(
                oldItem: PersonRecyclerViewItems,
                newItem: PersonRecyclerViewItems
        ) = when (oldItem) {
            is PersonRecyclerViewItems.MessageItem ->
                oldItem.personMessageItem ==
                        (newItem as PersonRecyclerViewItems.MessageItem).personMessageItem

        }
    }

    private val differ = AsyncListDiffer(this, messageDiffUtilItemCallback)

    fun setData(items: List<PersonRecyclerViewItems>) =
            differ.submitList(items)


    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = differ.currentList[position]) {
            is PersonRecyclerViewItems.MessageItem ->
                //detect message type
                if (item.personMessageItem.messageOwner == mainUser.id)
                    R.layout.sent_message
                else
                    R.layout.received_message

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //set appropriate viewHolder by view type
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


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]
        when (holder) {
            is SentMessageViewHolder -> {
                val message = item as PersonRecyclerViewItems.MessageItem
                holder.bind(message.personMessageItem)
            }
            is ReceivedMessageViewHolder -> {
                val message = item as PersonRecyclerViewItems.MessageItem
                holder.bind(message.personMessageItem)
            }
        }

    }

    inner class ReceivedMessageViewHolder(private val itemBinding: ReceivedMessageBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(personMessage: PersonMessage) {
            itemBinding.receivedMessageAivAvatar.visibility = ViewGroup.GONE
            itemBinding.tvChatMessage.text = personMessage.text
            itemBinding.tvChatTime.text = personMessage.userCreateTime.toLocalTime()
            itemBinding.cvChatMessage.setTailLength("left", 10f)
            //sent to fragment which item clicked and touch location
            itemView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        itemEventListener.onMessageItemClick(
                                personMessage,
                                v,
                                Point(event.x.toInt(), event.y.toInt())
                        )
                        v.performClick()
                    }
                }
                true
            }
        }
    }

    inner class SentMessageViewHolder(private val itemBinding: SentMessageBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(personMessage: PersonMessage) {
            itemBinding.tvChatMessage.text = personMessage.text
            itemBinding.cvChatMessage.setTailLength("right", 10f)
            itemBinding.lavMessageStatus.apply {
                if (personMessage.messageId.isNullOrEmpty()) {
                    itemBinding.tvChatTime.text = personMessage.userCreateTime.toLocalTime()
                    setAnimation(R.raw.message_watch)
                } else {
                    itemBinding.tvChatTime.text = personMessage.serverCreateTime!!.toLocalTime()
                    load(R.drawable.ic_check)
                }
            }
            //sent to fragment which item clicked and touch location
            itemView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        itemEventListener.onMessageItemClick(
                                personMessage,
                                v,
                                Point(event.x.toInt(), event.y.toInt())
                        )
                        v.performClick()
                    }
                }
                true
            }
        }
    }
}

/*
* currently person chat recycler view have message item but in future simply can
* add new element
* */
sealed class PersonRecyclerViewItems {
    class MessageItem(val personMessageItem: PersonMessage) :
            PersonRecyclerViewItems()
}