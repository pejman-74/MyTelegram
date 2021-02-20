package com.mytelegram.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.navArgs
import com.mytelegram.R
import com.mytelegram.data.model.resouces.ConnectionStatus
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.databinding.FragmentAcceptCodeBinding
import com.mytelegram.ui.base.BaseFragment
import com.mytelegram.util.handelApiError
import com.mytelegram.util.startWaitForConnectionAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AcceptCodeFragment : BaseFragment<AuthViewModel, FragmentAcceptCodeBinding>() {
    private val args: AcceptCodeFragmentArgs by navArgs()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //get formatted phone number  from login fragment
        vBinding.tvPhoneNumber.text = args.formatedPhoneNumber


        //handel connection status to change ui and error handling.
        vModel.connectionStatus.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled { connectionStatus ->
                when (connectionStatus) {
                    ConnectionStatus.Connect -> connectUiSetup()
                    ConnectionStatus.Disconnect, is ConnectionStatus.Error -> disconnectUiSetup()
                }
            }
        })

        //on user fill number field then send to server code validation request
        vBinding.civ.addOnCompleteListener { code ->
            vModel.codeValidation(args.rawPhoneNumber, code)
        }

        //handel code validation request
        vModel.codeValidationResponse.observe(viewLifecycleOwner, { eventResponse ->
            eventResponse.getContentIfNotHandled { response ->
                when (response) {
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        vModel.saveUser(response.value)
                    }
                    is Resource.Failure -> {
                        handelApiError(response)
                        vBinding.civ.setEditable(true)
                        vBinding.civ.code = ""
                    }
                }
            }
        })
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

    //because login and accept code fragment inside in nested graph used hiltNavGraphViewModels
    override fun getViewModel() =
            hiltNavGraphViewModels<AuthViewModel>(R.id.auth_graph)

    override fun getViewBinding(layoutInflater: LayoutInflater, container: ViewGroup?) =
            FragmentAcceptCodeBinding.inflate(layoutInflater, container, false)


}