package com.mytelegram.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.mytelegram.R
import com.mytelegram.data.model.resouces.ConnectionStatus
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.databinding.FragmentLoginBinding
import com.mytelegram.ui.base.BaseFragment
import com.mytelegram.util.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment : BaseFragment<AuthViewModel, FragmentLoginBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        connectToAuthServer()

        vBinding.ccp.registerCarrierNumberEditText(vBinding.etCarrierNumber)

        vBinding.pfabLogin.setOnClickListener {
            if (vBinding.ccp.isValidFullNumber) {
                vModel.login(
                    vBinding.ccp.fullNumberWithPlus,
                    vBinding.ccp.selectedCountryEnglishName
                )
                return@setOnClickListener
            }
            requireView().snackBar(getString(R.string.Phone_number_is_not_valid))
        }

        //handel connection status to change ui and error handling.
        vModel.connectionStatus.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled { connectionStatus ->
                when (connectionStatus) {
                    ConnectionStatus.Connect -> connectUiSetup()
                    ConnectionStatus.Disconnect -> disconnectUiSetup()
                    is ConnectionStatus.Error -> {
                        disconnectUiSetup()
                        if (connectionStatus.isAuth)
                            fatalAlertDialog(getString(R.string.server_reject_your_connection)) { connectToAuthServer() }
                    }
                }

            }
        })

        vModel.loginResponse.observe(viewLifecycleOwner, { eventResponse ->
            vBinding.pfabLogin.showLoadingAnimation(eventResponse.peekContent() is Resource.Loading)
            eventResponse.getContentIfNotHandled { response ->
                when (response) {
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        findNavController().navigate(
                            LoginFragmentDirections.actionLoginFragmentToAcceptCodeFragment(
                                vBinding.ccp.formattedFullNumber, vBinding.ccp.fullNumberWithPlus
                            )
                        )
                    }
                    is Resource.Failure -> handelApiError(response)
                }
            }
        })

    }
    //on connected to server set ui changes
    private fun connectUiSetup() {
        vBinding.ccp.isEnabled = true
        vBinding.pfabLogin.show()
        vBinding.toolbarTvStatus.apply {
            clearAnimation()
            text = getText(R.string.app_name)
        }

    }
    //on disconnected to server set ui changes
    private fun disconnectUiSetup() {
        vBinding.ccp.isEnabled = false
        vBinding.pfabLogin.hide()
        vBinding.toolbarTvStatus.apply {
            text = getString(R.string.connectiong)
            startWaitForConnectionAnimation()
        }
    }

    private fun connectToAuthServer() {
        vModel.connectToAuthServer(Utils.apiKey())
    }
    //because login and accept code fragment inside in nested graph used hiltNavGraphViewModels
    override fun getViewModel() =
        hiltNavGraphViewModels<AuthViewModel>(R.id.auth_graph)

    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLoginBinding.inflate(layoutInflater, container, false)


}