package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mobdeve.s12.mco.databinding.ActivityMainBinding

class MainActivity: AppCompatActivity() {

    private lateinit var viewBinding : ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navbar_fragment_controller) as NavHostFragment
        navController = navHostFragment.navController
        setupWithNavController(viewBinding.navbarNv, navController)

        viewBinding.navbarNv.setOnItemSelectedListener{item ->
            when(item.itemId) {
                R.id.action_home -> {
                    navController.navigate(R.id.action_home)
                    true
                }
                R.id.action_transactions -> {
                    navController.navigate(R.id.action_transactions)
                    true
                }
                R.id.action_favorites -> {
                    navController.navigate(R.id.action_favorites)
                    true
                }
                R.id.action_logout -> {
                    // TODO MCO3: Add secondary confirmation
                    finish()
                    true
                }

                else -> {
                    false
                }
            }
        }
    }
}