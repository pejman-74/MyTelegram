package com.mytelegram.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.mytelegram.R
import com.mytelegram.data.model.resouces.ConnectionStatus
import com.mytelegram.databinding.FragmentHomeBinding
import com.mytelegram.databinding.NavHeaderBinding
import com.mytelegram.ui.base.BaseFragment
import com.mytelegram.util.*
import com.mytelegram.util.custom_view.AvatarImageView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(), HomeFeedItemListener {
    private lateinit var homeFeedItemAdapter: HomeFeedItemAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        homeFeedItemAdapter = HomeFeedItemAdapter(this)

        val headerView = vBinding.navView.getHeaderView(0)
        val headerBinding = NavHeaderBinding.bind(headerView)
        headerBinding.navTvUserName.text = mainUser.userName
        headerBinding.navTvUserPhoneNumber.text = mainUser.phoneNumber
        headerBinding.navAimAvatar.apply {
            if (mainUser.profileUrl.isNullOrEmpty())
                text = getAvatarText(mainUser.userName)
            else {
                setShowState(AvatarImageView.SHOW_IMAGE)
                val avatarImageFile = File(context.getPictureDir(), "${mainUser.profileUrl}.jpg")
                if (avatarImageFile.exists())
                    load(avatarImageFile)
            }
        }

        vBinding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_create_group ->
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCreateGroup())
                R.id.menu_logout ->
                    (activity as HomeActivity).logOut()

            }
            vBinding.toolbarBtnNavigationDrawer.callOnClick()
            return@setNavigationItemSelectedListener true
        }

        vBinding.toolbarBtnSearch.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSearchDialogFragment())
        }

        vBinding.toolbarBtnNavigationDrawer.setOnClickListener {
            if (vBinding.drawerLayout.isDrawerOpen(GravityCompat.START))
                vBinding.drawerLayout.closeDrawer(GravityCompat.START)
            else
                vBinding.drawerLayout.openDrawer(GravityCompat.START)

        }

        vBinding.rvHome.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = homeFeedItemAdapter
        }

        vModel.homeFeed.observe(requireActivity(), { homeFeedItemAdapter.setData(it) })

        //handel connection status to change ui and error handling.
        vModel.connectionStatus.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled { connectionStatus ->
                when (connectionStatus) {
                    ConnectionStatus.Connect -> connectUiSetup()
                    ConnectionStatus.Disconnect -> disconnectUiSetup()
                    is ConnectionStatus.Error -> {
                        disconnectUiSetup()
                        if (connectionStatus.isAuth)
                            fatalAlertDialog(getString(R.string.server_reject_your_connection))
                    }
                }

            }
        })
    }

    override fun getViewModel() = activityViewModels<HomeViewModel>()

    override fun getViewBinding(
            layoutInflater: LayoutInflater,
            container: ViewGroup?
    ) = FragmentHomeBinding.inflate(layoutInflater, container, false)

    override fun longClick(roomOrUser: HomeRecyclerViewItemType) {

        when (roomOrUser) {
            is HomeRecyclerViewItemType.RoomItemHome -> {
                vModel.deleteRoom(roomOrUser.room.roomId)
            }
            is HomeRecyclerViewItemType.ConversationUserItemHome -> {
                vModel.deleteConversionUser(roomOrUser.cUser.userId)
            }
        }
    }

    //on connected to server set ui changes
    private fun connectUiSetup() {
        vBinding.toolbarTvStatus.apply {
            clearAnimation()
            text = getText(R.string.app_name)
        }

    }

    //on disconnected to server set ui changes
    private fun disconnectUiSetup() {
        vBinding.toolbarTvStatus.apply {
            text = getString(R.string.connectiong)
            startWaitForConnectionAnimation()
        }
    }
}