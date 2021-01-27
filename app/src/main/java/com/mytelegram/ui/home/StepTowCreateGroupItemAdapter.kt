package com.mytelegram.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mytelegram.R
import com.mytelegram.data.model.User
import com.mytelegram.databinding.SearchFeedItemBinding
import com.mytelegram.util.getAvatarText
import com.mytelegram.util.toLocalTime


class StepTowCreateGroupItemAdapter :
    RecyclerView.Adapter<StepTowCreateGroupItemAdapter.StepTowCreateGroupItemViewHolder>() {
    private val homeFeedItemDiffUtilItemCallback =
        object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(
                oldRecyclerViewItem: User,
                newRecyclerViewItem: User
            ) = oldRecyclerViewItem.userId == newRecyclerViewItem.userId

            override fun areContentsTheSame(
                oldRecyclerViewItem: User,
                newRecyclerViewItem: User
            ) = oldRecyclerViewItem == newRecyclerViewItem
        }
    private val differ = AsyncListDiffer(this, homeFeedItemDiffUtilItemCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StepTowCreateGroupItemViewHolder {
        return StepTowCreateGroupItemViewHolder(
                SearchFeedItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StepTowCreateGroupItemViewHolder, position: Int) {
        holder.bind(differ.currentList[holder.adapterPosition])
    }

    override fun getItemCount() = differ.currentList.size

    fun setData(recyclerViewItems: List<User>) {
        differ.submitList(recyclerViewItems)
    }

    inner class StepTowCreateGroupItemViewHolder(private val searchFeedItemBinding: SearchFeedItemBinding) :
        RecyclerView.ViewHolder(searchFeedItemBinding.root) {
        fun bind(user: User) {
            val context = searchFeedItemBinding.root.context
            searchFeedItemBinding.root.isClickable = false
            searchFeedItemBinding.tvHomeSearchUserName.text = user.userName
            user.lastSeen?.let {
                searchFeedItemBinding.tvHomeFeedItemDescription.text =
                        if (it == "on") context.getText(R.string.online)
                        else context.getString(R.string.last_seen_at, it.toLocalTime())

            }
            searchFeedItemBinding.aimSearchFeedAvatar.apply {
                if (user.profileUrl.isNullOrEmpty())
                    text = getAvatarText(user.userName)
                else
                    load(user.profileUrl)
            }
        }
    }

}


