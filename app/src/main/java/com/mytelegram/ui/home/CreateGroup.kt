package com.mytelegram.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.mytelegram.data.model.User
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.databinding.FragmentCreateGroupBinding
import com.mytelegram.ui.base.BaseFragment
import com.mytelegram.util.handelApiError
import com.mytelegram.util.hideKeyboard
import com.mytelegram.util.showKeyboard


class CreateGroup : BaseFragment<HomeViewModel, FragmentCreateGroupBinding>(),
        CreateGroupSearchFeedItemListener {
    private val selectedUsers = ArrayList<User>()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vBinding.toolbarBtnBack.setOnClickListener {
            parentFragment?.findNavController()?.navigateUp()
        }

        val searchItemAdapter = CreateGroupSearchItemAdapter(this)
        //send selected user to next step fragment
        vBinding.fabCreateGroupStepOne.setOnClickListener {
            findNavController().navigate(
                    CreateGroupDirections.actionCreateGroupToCreateGroupStepTwoFragment(
                            selectedUsers.toTypedArray()
                    )
            )
        }

        vBinding.fabCreateGroupStepOne.visibility = ViewGroup.GONE
        vBinding.rvCreateGroup.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = searchItemAdapter
        }
        vBinding.itSearch.showKeyboard()

        //search in users to create new group
        vBinding.itSearch.doOnTextChanged { text, _, _, _ ->

            if (text.toString().isNotBlank())
                vModel.searchUser(text.toString())
                        .observe(viewLifecycleOwner, { resource ->
                            when (resource) {
                                is Resource.Success -> {
                                    val users = resource.value.users.toMutableList()
                                    selectedUsers.forEach { _user ->
                                        users.removeAll { user ->
                                            user.userId == _user.userId
                                        }
                                    }
                                    searchItemAdapter.setData(users)
                                }
                                is Resource.Failure -> handelApiError(resource)
                                Resource.Loading -> TODO()
                            }
                        })
            else
                searchItemAdapter.clearData()
        }
    }


    override fun getViewModel() = activityViewModels<HomeViewModel>()

    override fun getViewBinding(layoutInflater: LayoutInflater, container: ViewGroup?) =
            FragmentCreateGroupBinding.inflate(layoutInflater, container, false)

    override fun onUserItemClick(user: User) {
        val chip = Chip(requireContext())
        chip.text = user.userName
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            vBinding.chipGroup.removeView(it)
            selectedUsers.remove(user)
            if (selectedUsers.isEmpty())
                vBinding.fabCreateGroupStepOne.visibility = ViewGroup.GONE
        }
        vBinding.fabCreateGroupStepOne.visibility = ViewGroup.VISIBLE
        vBinding.chipGroup.addView(chip)
        selectedUsers.add(user)
        vBinding.itSearch.text?.clear()
    }

    override fun onPause() {
        super.onPause()
        vBinding.itSearch.hideKeyboard()
    }
}