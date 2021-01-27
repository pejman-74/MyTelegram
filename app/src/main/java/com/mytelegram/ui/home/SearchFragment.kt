package com.mytelegram.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.databinding.SearchFragmentBinding
import com.mytelegram.ui.base.BaseFragment
import com.mytelegram.util.handelApiError
import com.mytelegram.util.hideKeyboard
import com.mytelegram.util.showKeyboard
import com.mytelegram.util.toConservationUser
import kotlinx.coroutines.launch


class SearchFragment : BaseFragment<HomeViewModel, SearchFragmentBinding>(),
        SearchFeedItemListener {


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val searchItemAdapter = SearchFeedItemAdapter(this)

        vBinding.toolbarBtnBack.setOnClickListener {
            parentFragment?.findNavController()?.navigateUp()
        }
        vBinding.itSearch.showKeyboard()

        vBinding.rvSearchDialog.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = searchItemAdapter
        }

        vBinding.itSearch.doOnTextChanged { text, _, _, _ ->

            if (text.toString().isNotBlank())
                vModel.search(text.toString())
                        .observe(viewLifecycleOwner, { resource ->
                            when (resource) {
                                is Resource.Success -> {
                                    val searchResponse = resource.value
                                    val homeFeedRecyclerViewItem =
                                            ArrayList<SearchFeedItem>()
                                    searchResponse.rooms.forEach {
                                        homeFeedRecyclerViewItem.add(
                                                SearchFeedItem(it.roomId, SearchRecyclerViewItemType.RoomItemHome(it)))
                                    }
                                    searchResponse.users.forEach {
                                        homeFeedRecyclerViewItem.add(
                                                SearchFeedItem(it.userId, SearchRecyclerViewItemType.UserItemHome(it)))
                                    }
                                    searchItemAdapter.setData(homeFeedRecyclerViewItem)
                                }
                                is Resource.Failure -> handelApiError(resource)
                                Resource.Loading -> Unit
                            }
                        })
            else
                searchItemAdapter.clearData()
        }
    }


    override fun getViewModel() = activityViewModels<HomeViewModel>()

    override fun getViewBinding(layoutInflater: LayoutInflater, container: ViewGroup?) =
            SearchFragmentBinding.inflate(layoutInflater, container, false)

    override fun onItemTouch(itemHome: SearchRecyclerViewItemType) {
        view?.hideKeyboard()
        when (itemHome) {
            is SearchRecyclerViewItemType.UserItemHome -> {
                findNavController()
                        .navigate(SearchFragmentDirections.actionGlobalPersonChatFragment(itemHome.user.toConservationUser()))
            }
            is SearchRecyclerViewItemType.RoomItemHome -> {
                lifecycleScope.launch {
                    val room = vModel.getRoom(itemHome.room.roomId)
                    if (room == null)
                        findNavController().navigate(SearchFragmentDirections.actionSearchToGroupPreview(itemHome.room))
                    else
                        findNavController().navigate(SearchFragmentDirections.actionGlobalGroupChatFragment(itemHome.room))
                }

            }
        }


    }
}