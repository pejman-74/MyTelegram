package com.mytelegram.ui.home

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.mytelegram.R
import com.mytelegram.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

/*
* In home activity observing the main user if main user exist navigate to home fragment
* else navigate to auth_graph(nested graph)
* */
    private val vModel by viewModels<HomeViewModel>()
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var graph: NavGraph

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        val binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        navHostFragment =
            supportFragmentManager.findFragmentById(binding.nhfHome.id) as NavHostFragment
        graph = navHostFragment.navController.navInflater.inflate(R.navigation.home_nav)
        navController = navHostFragment.navController



        vModel.getLiveMainUser.observeForever { event ->
            event.getContentIfNotHandled { mainUser ->
                if (mainUser != null) {
                    setHomeFragment(R.id.homeFragment)
                    vModel.connectToUserServer(mainUser.lastAuthToken)
                } else
                    setHomeFragment(R.id.auth_graph)
            }
        }

    }

    fun logOut() {
        vModel.logout()
        vModel.disconnectFromUserServer()
    }

    //set start destination of graph then set that to nav host fragment
    private fun setHomeFragment(@IdRes id: Int) {
        graph.startDestination = id
        navHostFragment.navController.graph = graph
    }


}