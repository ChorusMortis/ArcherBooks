package com.mobdeve.s12.mco

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.mobdeve.s12.mco.databinding.ActivityMainBinding
import com.mobdeve.s12.mco.databinding.ComponentDialogConfirmlogoutBinding

class MainActivity: AppCompatActivity() {

    private lateinit var viewBinding : ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // handle bottom navigation view to control fragments
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navbar_fragment_controller) as NavHostFragment
        navController = navHostFragment.navController
        setupWithNavController(viewBinding.navbarNv, navController)
        addListenersBottomNavBarItems()

        // reset search preferences after app closes to default
        removeSearchPreferences()
    }

    private fun addListenersBottomNavBarItems() {
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
                    showConfirmLogoutDialog()
                    // don't highlight logout button
                    false
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun showConfirmLogoutDialog() {
        val confirmLogoutDialogBinding =
            ComponentDialogConfirmlogoutBinding.inflate(LayoutInflater.from(this))
        // use custom style to force dialog to wrap content and not take up entire screen's width
        val dialog = AlertDialog.Builder(this,  R.style.WrapContentDialog)
            .setView(confirmLogoutDialogBinding.root)
            .setCancelable(false)
            .create()

        // make background transparent so dialog floats
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmLogoutDialogBinding.dialogConfirmlogoutBtnCancel.setOnClickListener {
            dialog.dismiss()
        }

        confirmLogoutDialogBinding.dialogConfirmlogoutBtnConfirm.setOnClickListener {
            dialog.dismiss()
            // finish main activity (all fragments)
            val authHandler = AuthHandler.getInstance(this)
            authHandler.logoutAccount()
            finish()
        }

        dialog.show()
    }

    private fun removeSearchPreferences() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()

        editor.remove(SearchResultsFragment.SEARCH_RESULTS_SORT_PREF)
        editor.remove(SearchResultsFragment.SEARCH_RESULTS_FILTER_PREF)

        editor.remove(TransactionsFragment.TRANSACTIONS_SORT_PREF)
        editor.remove(TransactionsFragment.TRANSACTIONS_FILTER_PREF)

        editor.remove(FavoritesFragment.FAVORITES_SORT_PREF)
        editor.remove(FavoritesFragment.FAVORITES_FILTER_PREF)

        editor.apply()
    }
}