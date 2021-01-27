package com.mytelegram.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mytelegram.R
import com.mytelegram.data.model.Room
import com.mytelegram.data.model.User
import com.mytelegram.databinding.SearchFeedItemBinding
import com.mytelegram.util.custom_view.AvatarImageView
import com.mytelegram.util.getAvatarText
import com.mytelegram.util.getPictureDir
import com.mytelegram.util.toLocalTime
import java.io.File

interface SearchFeedItemListener {
    fun onItemTouch(itemHome: SearchRecyclerViewItemType)

}


class SearchFeedItemAdapter(
        private val itemListener: SearchFeedItemListener
) : RecyclerView.Adapter<SearchFeedItemAdapter.SearchFeedItemViewHolder>() {
    private val searchItemDiffUtilItemCallback =
            object : DiffUtil.ItemCallback<SearchFeedItem>() {
                override fun areItemsTheSame(
                        oldRecyclerViewItem: SearchFeedItem,
                        newRecyclerViewItem: SearchFeedItem
                ) = oldRecyclerViewItem.id == newRecyclerViewItem.id

                override fun areContentsTheSame(
                        oldRecyclerViewItem: SearchFeedItem,
                        newRecyclerViewItem: SearchFeedItem
                ) = oldRecyclerViewItem == newRecyclerViewItem
            }
    private val differ = AsyncListDiffer(this, searchItemDiffUtilItemCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchFeedItemViewHolder {

        return SearchFeedItemViewHolder(
                SearchFeedItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: SearchFeedItemViewHolder, position: Int) {
        holder.bind(differ.currentList[position])

        holder.itemView.setOnClickListener {
            itemListener.onItemTouch(differ.currentList[position].itemHome)
        }
    }

    inner class SearchFeedItemViewHolder(private val searchFeedItemBinding: SearchFeedItemBinding) :
            RecyclerView.ViewHolder(searchFeedItemBinding.root) {
        fun bind(recyclerViewItem: SearchFeedItem) {
            val context = searchFeedItemBinding.root.context
            when (val item = recyclerViewItem.itemHome) {
                is SearchRecyclerViewItemType.RoomItemHome -> {
                    searchFeedItemBinding.tvHomeSearchUserName.text = item.room.name
                    searchFeedItemBinding.tvHomeFeedItemDescription.text =
                            itemView.context.getString(R.string.members, item.room.count_members)
                    searchFeedItemBinding.aimSearchFeedAvatar.apply {
                        if (item.room.avatar_url.isNullOrEmpty())
                            text = getAvatarText(item.room.name)
                        else {
                            setShowState(AvatarImageView.SHOW_IMAGE)
                            val avatarImageFile = File(
                                    context.getPictureDir(),
                                    "${item.room.avatar_url}.jpg"
                            )
                            if (avatarImageFile.exists())
                                load(avatarImageFile)
                        }

                    }
                }
                is SearchRecyclerViewItemType.UserItemHome -> {
                    searchFeedItemBinding.tvHomeSearchUserName.text = item.user.userName
                    item.user.lastSeen?.let {
                        searchFeedItemBinding.tvHomeFeedItemDescription.text =
                                if (it == "on") context.getText(R.string.online) else
                                    context.getString(R.string.last_seen_at, it.toLocalTime())

                    }
                    searchFeedItemBinding.aimSearchFeedAvatar.apply {
                        if (item.user.profileUrl.isNullOrBlank())
                            text = getAvatarText(item.user.userName)
                        else {
                            setShowState(AvatarImageView.SHOW_IMAGE)
                            val avatarImageFile = File(
                                    context.getPictureDir(),
                                    "${item.user.profileUrl}.jpg"
                            )
                            if (avatarImageFile.exists())
                                load(avatarImageFile)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount() = differ.currentList.size

    fun setData(recyclerViewItems: List<SearchFeedItem>) =
            differ.submitList(recyclerViewItems)


    fun clearData() {
        differ.submitList(null)
        notifyDataSetChanged()
    }
}

data class SearchFeedItem(
        val id: String,
        val itemHome: SearchRecyclerViewItemType,
)
sealed class SearchRecyclerViewItemType {
    class RoomItemHome(val room: Room) : SearchRecyclerViewItemType()
    class UserItemHome(val user: User) : SearchRecyclerViewItemType()
}