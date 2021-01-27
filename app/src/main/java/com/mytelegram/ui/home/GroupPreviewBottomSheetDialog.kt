package com.mytelegram.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mytelegram.R
import com.mytelegram.data.model.User
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.databinding.GroupPreviewDialogBottomSheetBinding
import com.mytelegram.databinding.GroupPreviewMemberItemBinding
import com.mytelegram.util.custom_view.AvatarImageView
import com.mytelegram.util.getAvatarText
import com.mytelegram.util.getPictureDir
import com.mytelegram.util.handelApiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/*
* Show preview of groups, user can see sample of users count of members
* */
class GroupPreviewBottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var vBinding: GroupPreviewDialogBottomSheetBinding
    private val args: GroupPreviewBottomSheetDialogArgs by navArgs()
    private val vModel: HomeViewModel by activityViewModels()

    override fun getTheme() = R.style.CustomBottomSheetDialog


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        vBinding = GroupPreviewDialogBottomSheetBinding.inflate(inflater, container, false)
        return vBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.launch {
            when (val result = vModel.getRoomSampleMembers(args.room.roomId)) {
                is Resource.Success -> {
                    val users = ArrayList<User>()
                    val room = result.value
                    room.owner?.let { users.add(it) }
                    room.admins?.let { users.addAll(it) }
                    room.members?.let { users.addAll(it) }
                    vBinding.groupPreviewRvMembers.apply {
                        setHasFixedSize(true)
                        adapter = GroupPreviewMemberItemAdapter(users)
                    }
                }
                is Resource.Failure -> TODO()
                Resource.Loading -> TODO()
            }

        }

        vBinding.groupPreviewTvMemberCount.text =
                getString(R.string.members, args.room.count_members)
        vBinding.groupPreviewTvName.text = args.room.name
        vBinding.groupPreviewTvCancel.setOnClickListener {
            this.dismiss()
        }
        vBinding.groupPreviewAiv.apply {
            val avatarImageFile = File(
                    context.getPictureDir(),
                    "${args.room.avatar_url}.jpg"
            )
            if (args.room.avatar_url.isNullOrEmpty() || !avatarImageFile.exists())
                text = getAvatarText(args.room.name)
            else {
                setShowState(AvatarImageView.SHOW_IMAGE)
                load(avatarImageFile)
            }

        }
        vBinding.groupPreviewTvJoin.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                when (val result = vModel.joinToRoom(args.room.roomId)) {
                    is Resource.Success -> {
                        findNavController().navigate(SearchFragmentDirections.actionGlobalGroupChatFragment(args.room))
                    }
                    is Resource.Failure -> handelApiError(result)
                    Resource.Loading -> Unit
                }

            }
        }
    }

    class GroupPreviewMemberItemAdapter(private val users: List<User>) :
            RecyclerView.Adapter<GroupPreviewMemberItemAdapter.GroupPreviewMemberItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                GroupPreviewMemberItemViewHolder(
                        GroupPreviewMemberItemBinding.inflate(
                                LayoutInflater.from(parent.context), parent, false
                        )
                )


        override fun onBindViewHolder(holder: GroupPreviewMemberItemViewHolder, position: Int) {
            holder.bind(users[holder.adapterPosition])
        }

        override fun getItemCount() = users.size

        inner class GroupPreviewMemberItemViewHolder(private val groupPreviewMemberItemBinding: GroupPreviewMemberItemBinding) :
                RecyclerView.ViewHolder(groupPreviewMemberItemBinding.root) {
            fun bind(user: User) {
                groupPreviewMemberItemBinding.groupPreviewMemberTvUserName.text = user.userName

                groupPreviewMemberItemBinding.groupPreviewMemberAivItem.apply {
                    if (user.profileUrl.isNullOrBlank())
                        text = getAvatarText(user.userName)
                    else {
                        setShowState(AvatarImageView.SHOW_IMAGE)
                        val profileImageFile = File(context.getPictureDir(),
                                "${user.profileUrl}.jpg")
                        if (!profileImageFile.exists())
                            load(profileImageFile)
                    }
                }
            }
        }
    }
}