package com.mytelegram.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VM : ViewModel, VB : ViewBinding> : Fragment() {

    lateinit var vBinding: VB
    lateinit var vModel: VM
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel = getViewModel().value
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vBinding = getViewBinding(inflater, container)
        return vBinding.root
    }

    abstract fun getViewModel(): Lazy<VM>
    abstract fun getViewBinding(layoutInflater: LayoutInflater, container: ViewGroup?): VB


}

