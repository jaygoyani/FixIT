package com.android.fixit.activities

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.android.fixit.R
import com.android.fixit.databinding.ActivityUserMainBinding
import com.android.fixit.databinding.ActivityWorkerMainBinding
import com.android.fixit.managers.PrefManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.android.fixit.utils.Navigator
import com.google.firebase.auth.FirebaseAuth

class WorkerMainActivity : AppCompatActivity() {
    private val context: Context = this
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityWorkerMainBinding
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.worker_container)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_worker_home, R.id.nav_worker_jobs, R.id.nav_profile
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        initialise()
        setListeners()
    }

    private fun setListeners() {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_logout) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirmation")
            builder.setMessage("Are you sure that you want to logout?")
            builder.setPositiveButton("YES") { _: DialogInterface?, _: Int ->
                logout()
            }
            builder.setNegativeButton("NO") { _: DialogInterface?, _: Int -> }
            builder.show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        Helper.unSubscribeToTopic(Helper.getTopic(PrefManager.getUserDTO()))
        PrefManager.clearUserPreferences()
        FirebaseAuth.getInstance().signOut()
        Navigator.toLoginActivity()
        finish()
    }

    private fun initialise() {
        val user = PrefManager.getUserDTO()
        val headerView = binding.navView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.name).text = user.name
        headerView.findViewById<TextView>(R.id.info).text =
            "${Helper.formatRole(user.role)} ${Constants.DOT} ${user.countryCode}-${user.mobileNumber}"
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to exit?")
        builder.setPositiveButton("YES") { _: DialogInterface?, _: Int -> finish() }
        builder.setNegativeButton("NO") { _: DialogInterface?, _: Int -> }
        builder.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}