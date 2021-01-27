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
import com.mytelegram.data.model.ConversationUser
import com.mytelegram.data.model.PersonMessage
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.databinding.FragmentPersonChatBinding
import com.mytelegram.databinding.MessagePopupMenuBinding
import com.mytelegram.ui.base.BaseFragment
import com.mytelegram.util.*
import com.mytelegram.util.custom_view.AvatarImageView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PersonChatFragment : BaseFragment<ChatViewModel, FragmentPersonChatBinding>(),
        PersonMessageItemListener {

    private lateinit var personMessageItemAdapter: PersonMessageItemAdapter
    private val args: PersonChatFragmentArgs by navArgs()
    private lateinit var popupWindow: PopupWindow
    private lateinit var messagePopupMenuBinding: MessagePopupMenuBinding
    private var editModeMessage: PersonMessage? = null

    private fun updateUserUI(cUser: ConversationUser) {
        //if user is online show "online" else convert last online time to local time then show them
        cUser.lastSeen?.let {
            vBinding.toolbarTvTitleDescription.text = if (it == "on") getText(R.string.online)
            else getString(R.string.last_seen_at, it.toLocalTime())
        }
        vBinding.toolbarTvTitle.text = cUser.userName
        //if user have't avatar show user name in avatar
        vBinding.toolbarAimAvatar.apply {
            if (cUser.profileUrl.isNullOrEmpty())
                text = getAvatarText(cUser.userName)
            else {
                setShowState(AvatarImageView.SHOW_IMAGE)
                load(
                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.toURI()
                                .toString() + "${cUser.profileUrl}.jpg"
                )
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        updateUserUI(args.user)

        personMessageItemAdapter = PersonMessageItemAdapter(this)

        vBinding.rvChat.apply {
            setHasFixedSize(true)
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = personMessageItemAdapter
        }


        vModel.getConversationUserWithPersonMessage(args.user.userId)
        vModel.conversationUserWithPersonMessage.observe(
                viewLifecycleOwner,
                { cuWithPm ->
                    updateUserUI(cuWithPm.conversationUser)
                    //map sent person messages to recycler view item
                    val sentMessage = cuWithPm.sentPersonMessages.map {
                        PersonRecyclerViewItems.MessageItem(it)
                    }
                    //map received person messages to recycler view item
                    val receivedMessage = cuWithPm.receivedPersonMessages.map {
                        PersonRecyclerViewItems.MessageItem(it)
                    }
                    //then plus sent and received messages then sort by user create time
                    val sortAndJoin = sentMessage.plus(receivedMessage)
                            .sortedBy { it.personMessageItem.userCreateTime }

                    personMessageItemAdapter.setData(sortAndJoin)
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
                vModel.editPersonMessage(it, message).observe(viewLifecycleOwner, { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            vBinding.messageSendEditLayout.tiMessage.text?.clear()
                            exitEditMode()
                        }
                        is Resource.Failure -> handelApiError(resource)
                        Resource.Loading -> Unit
                    }
                })
                return@setOnClickListener
            }
            vModel.sendPersonMessage(
                    PersonMessage(
                            text = message, userCreateTime = getCurrentUTCDateTime(),
                            messageOwner = mainUser.id,
                            receiverUser = args.user.userId
                    ), args.user
            )
            vBinding.messageSendEditLayout.tiMessage.text?.clear()
        }

        initMessagePopupMenu()
    }

    private fun initMessagePopupMenu() {
        messagePopupMenuBinding = MessagePopupMenuBinding.inflate(layoutInflater)
        popupWindow = PopupWindow(
                messagePopupMenuBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false
        )

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

    private fun setEditMode(personMessage: PersonMessage) {
        vBinding.messageSendEditLayout.editMessageClose.setOnClickListener {
            exitEditMode()
        }
        vBinding.messageSendEditLayout.editMessageLayout.visibility = ViewGroup.VISIBLE
        vBinding.messageSendEditLayout.preMessageText.text = personMessage.text
        editModeMessage = personMessage
    }

    /*
        if clicked message the owners equals with the main user
        show all popup menu items else just user can use copy item
        */
    override fun onMessageItemClick(
            personMessage: PersonMessage,
            view: View,
            point: Point
    ) {
        if (personMessage.messageOwner == mainUser.id) {
            messagePopupMenuBinding.messageMenuEdit.visibility = ViewGroup.VISIBLE
            messagePopupMenuBinding.messageMenuEdit.setOnClickListener {
                popupWindow.dismiss()
                setEditMode(personMessage)
            }
        } else {
            messagePopupMenuBinding.messageMenuEdit.visibility = ViewGroup.GONE
        }
        messagePopupMenuBinding.messageMenuDelete.setOnClickListener {
            popupWindow.dismiss()
            vModel.deletePersonMessage(personMessage)
        }
        messagePopupMenuBinding.messageMenuCopy.setOnClickListener {
            val clipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(
                    ClipData.newPlainText(
                            getString(R.string.app_name),
                            personMessage.text
                    )
            )
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
            FragmentPersonChatBinding.inflate(layoutInflater, container, false)

}

