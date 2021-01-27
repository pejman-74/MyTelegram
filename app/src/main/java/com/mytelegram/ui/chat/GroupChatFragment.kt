package com.mytelegram.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.PopupWindow
import androidx.core.widget.PopupWindowCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.mytelegram.R
import com.mytelegram.data.model.RoomMessage
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.databinding.FragmentGroupChatBinding
import com.mytelegram.databinding.MessagePopupMenuBinding
import com.mytelegram.ui.base.BaseFragment
import com.mytelegram.util.custom_view.AvatarImageView
import com.mytelegram.util.getAvatarText
import com.mytelegram.util.getCurrentUTCDateTime
import com.mytelegram.util.mainUser
import com.mytelegram.util.showLongToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GroupChatFragment : BaseFragment<ChatViewModel, FragmentGroupChatBinding>(),
        RoomMessageItemListener {

    private lateinit var roomMessageAdapterAdapter: RoomMessageItemAdapter
    private val args: GroupChatFragmentArgs by navArgs()
    private lateinit var popupWindow: PopupWindow
    private lateinit var messagePopupMenuBinding: MessagePopupMenuBinding
    private var editModeMessage: RoomMessage? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        roomMessageAdapterAdapter = RoomMessageItemAdapter(this)

        vBinding.rvChat.apply {
            setHasFixedSize(true)
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = roomMessageAdapterAdapter
        }
        vBinding.toolbarTvTitle.text = args.room.name

        //if room have't avatar show room name in avatar
        vBinding.toolbarAimAvatar.apply {
            if (args.room.avatar_url.isNullOrEmpty())
                text = getAvatarText(args.room.name)
            else {
                setShowState(AvatarImageView.SHOW_IMAGE)
                load(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.toURI()
                        .toString() + "${args.room.avatar_url}.jpg")
            }
        }

        vModel.getRoomWithMessagesAndUsers(args.room.roomId)

        vModel.roomWithMessagesAndUsers.observe(viewLifecycleOwner, {
            //show count of members in title bar
            vBinding.toolbarTvTitleDescription.text = getString(R.string.members, it.roomUsers.size)
            //first set user members in adapters
            roomMessageAdapterAdapter.setUserData(it.roomUsers)
            //map room messages to recycler view message item then set
            val messages = it.roomMessages.map { message ->
                GroupRecyclerViewItems.MessageItem(message)
            }
            roomMessageAdapterAdapter.setMessageData(messages)

        })

        vBinding.toolbarBtnBack.setOnClickListener { findNavController().navigateUp() }

        vBinding.messageSendEditLayout.btnSendMessage.isEnabled = false
        vBinding.messageSendEditLayout.tiMessage.doOnTextChanged { _, _, _, count ->
            vBinding.messageSendEditLayout.btnSendMessage.isEnabled = count > 0
        }

        // if in edit mode send edit message request else send new message request
        vBinding.messageSendEditLayout.btnSendMessage.setOnClickListener {
            val message = vBinding.messageSendEditLayout.tiMessage.text.toString()
            editModeMessage?.let {
                vModel.editRoomMessage(it, message).observe(viewLifecycleOwner, { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            vBinding.messageSendEditLayout.tiMessage.text?.clear()
                            exitEditMode()
                        }
                        is Resource.Failure -> TODO()
                        Resource.Loading -> TODO()
                    }
                })
                return@setOnClickListener
            }
            vBinding.messageSendEditLayout.tiMessage.text?.clear()
            vModel.sendRoomMessage(
                    RoomMessage(
                            text = message, userCreateTime = getCurrentUTCDateTime(),
                            messageOwner = mainUser.id, roomOwner = args.room.roomId)
            )
        }

        initMessagePopupMenu()
    }


    private fun initMessagePopupMenu() {
        messagePopupMenuBinding = MessagePopupMenuBinding.inflate(layoutInflater)
        popupWindow = PopupWindow(messagePopupMenuBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false)
        popupWindow.apply {
            setBackgroundDrawable(ColorDrawable())
            isTouchable = true
            isOutsideTouchable = true
            PopupWindowCompat.setOverlapAnchor(this, true)
        }


    }

    private fun exitEditMode() {
        vBinding.messageSendEditLayout.editMessageLayout.visibility = ViewGroup.GONE
        editModeMessage = null
    }

    private fun setEditMode(roomMessage: RoomMessage) {
        vBinding.messageSendEditLayout.editMessageClose.setOnClickListener {
            exitEditMode()
        }
        vBinding.messageSendEditLayout.editMessageLayout.visibility = ViewGroup.VISIBLE
        vBinding.messageSendEditLayout.preMessageText.text = roomMessage.text
        editModeMessage = roomMessage
    }

    /*
        if clicked message the owners equals with the main user
        show all popup menu items else just user can use copy item
        */
    override fun onMessageItemClick(roomMessage: RoomMessage, view: View, point: Point) {

        if (roomMessage.messageOwner == mainUser.id) {
            messagePopupMenuBinding.messageMenuEdit.visibility = ViewGroup.VISIBLE
            messagePopupMenuBinding.messageMenuDelete.visibility = ViewGroup.VISIBLE
            messagePopupMenuBinding.messageMenuEdit.setOnClickListener {
                popupWindow.dismiss()
                setEditMode(roomMessage)
            }
            messagePopupMenuBinding.messageMenuDelete.setOnClickListener {
                popupWindow.dismiss()
                vModel.deleteRoomMessage(roomMessage)
            }
        } else {
            messagePopupMenuBinding.messageMenuEdit.visibility = ViewGroup.GONE
            messagePopupMenuBinding.messageMenuDelete.visibility = ViewGroup.GONE
        }

        messagePopupMenuBinding.messageMenuCopy.setOnClickListener {
            val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), roomMessage.text))
            requireContext().showLongToast(getString(R.string.copied))
            popupWindow.dismiss()
        }
        PopupWindowCompat.showAsDropDown(popupWindow, view, point.x, point.y, Gravity.NO_GRAVITY)

    }

    override fun onPause() {
        popupWindow.dismiss()
        super.onPause()
    }

    override fun getViewModel() = activityViewModels<ChatViewModel>()

    override fun getViewBinding(layoutInflater: LayoutInflater, container: ViewGroup?) =
            FragmentGroupChatBinding.inflate(layoutInflater, container, false)
}