package com.mytelegram.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mytelegram.data.model.ConversationUser
import com.mytelegram.data.model.Room
import com.mytelegram.databinding.HomeFeedItemBinding
import com.mytelegram.util.custom_view.AvatarImageView
import com.mytelegram.util.getAvatarText
import com.mytelegram.util.getPictureDir
import java.io.File

interface HomeFeedItemListener {
    fun longClick(roomOrUser: HomeRecyclerViewItemType)
}

class HomeFeedItemAdapter(
        private val itemEventListener: HomeFeedItemListener
) : RecyclerView.Adapter<HomeFeedItemAdapter.HomeFeedItemViewHolder>() {

    private val homeFeedItemDiffUtilItemCallback =
            object : DiffUtil.ItemCallback<HomeFeedItem>() {
                override fun areItemsTheSame(
                        oldRecyclerViewItem: HomeFeedItem,
                        newRecyclerViewItem: HomeFeedItem
                ) = oldRecyclerViewItem.id == newRecyclerViewItem.id

                override fun areContentsTheSame(
                        oldRecyclerViewItem: HomeFeedItem,
                        newRecyclerViewItem: HomeFeedItem
                ) = oldRecyclerViewItem == newRecyclerViewItem
            }
    private val differ = AsyncListDiffer(this, homeFeedItemDiffUtilItemCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFeedItemViewHolder {
        return HomeFeedItemViewHolder(
                HomeFeedItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: HomeFeedItemViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
        holder.itemView.setOnClickListener {
            when (val item = differ.currentList[position].itemHome) {
                is HomeRecyclerViewItemType.ConversationUserItemHome -> {
                    it.findNavController()
                            .navigate(HomeFragmentDirections.actionGlobalPersonChatFragment(item.cUser))
                }
                is HomeRecyclerViewItemType.RoomItemHome -> {
                    it.findNavController()
                            .navigate(HomeFragmentDirections.actionGlobalGroupChatFragment(item.room))
                }
            }

        }
        holder.itemView.setOnLongClickListener {
            itemEventListener.longClick(differ.currentList[position].itemHome)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount() = differ.currentList.size

    fun setData(recyclerViewItems: List<HomeFeedItem>) =
            differ.submitList(recyclerViewItems)


    inner class HomeFeedItemViewHolder(private val homeFeedItemBinding: HomeFeedItemBinding) :
            RecyclerView.ViewHolder(homeFeedItemBinding.root) {
        fun bind(recyclerViewItem: HomeFeedItem) {

            when (val roomOrUser = recyclerViewItem.itemHome) {
                is HomeRecyclerViewItemType.RoomItemHome -> {
                    homeFeedItemBinding.tvHomeFeedUserName.text = roomOrUser.room.name
                    homeFeedItemBinding.aimHomeFeedAvatar.apply {
                        if (roomOrUser.room.avatar_url.isNullOrEmpty())
                            text = getAvatarText(roomOrUser.room.name)
                        else {
                            setShowState(AvatarImageView.SHOW_IMAGE)
                            val avatarImageFile = File(context.getPictureDir(),
                                    "${roomOrUser.room.avatar_url}.jpg")
                            if (avatarImageFile.exists())
                                load(avatarImageFile)
                        }

                    }
                }
                is HomeRecyclerViewItemType.ConversationUserItemHome -> {
                    homeFeedItemBinding.tvHomeFeedUserName.text = roomOrUser.cUser.userName
                    homeFeedItemBinding.aimHomeFeedAvatar.apply {
                        if (roomOrUser.cUser.profileUrl.isNullOrBlank())
                            text = getAvatarText(roomOrUser.cUser.userName)
                        else {
                            setShowState(AvatarImageView.SHOW_IMAGE)
                            val avatarImageFile = File(
                                    context.getPictureDir(),
                                    "${roomOrUser.cUser.profileUrl}.jpg"
                            )

                            if (avatarImageFile.exists())
                                load(avatarImageFile)
                        }
                    }
                }
            }
            homeFeedItemBinding.tvHomeFeedLastMessage.text = recyclerViewItem.lastMessage
        }
    }
}

data class HomeFeedItem(
        val id: String,
        val itemHome: HomeRecyclerViewItemType,
        val lastMessage: String? = null,
)

sealed class HomeRecyclerViewItemType {
    class RoomItemHome(val room: Room) : HomeRecyclerViewItemType()
    class ConversationUserItemHome(val cUser: ConversationUser) : HomeRecyclerViewItemType()
}