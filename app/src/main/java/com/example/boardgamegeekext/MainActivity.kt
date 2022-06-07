package com.example.boardgamegeekext

import android.app.PendingIntent.getActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation : BottomNavigationView

    private val homeFragment = HomeFragment()
    private val settingsFragment = SettingsFragment()
    private val listFragment = ListFragment()

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("UWU2",savedInstanceState?.size().toString() )

//        val intent = Intent(this, InitialConfiguration::class.java)
//        startActivity(intent)

//        val dbHandler = DatabaseHelper(this, null, null, 1)
//        val user = User("Marcin", "marcol13")
//
//        dbHandler.addUser(user)



        bottomNavigation = findViewById(R.id.bottom_navigation)

        setCurrentFragment(homeFragment)

//        R.id.nav_home.onNavDestinationSelected()

        bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> setCurrentFragment(homeFragment)
                R.id.nav_settings -> setCurrentFragment(settingsFragment)
                R.id.nav_list -> setCurrentFragment(listFragment)
            }
            true
        })

//        bottomNavigation.onNavDest

        savedInstanceState?.clear();
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
//    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment)
            commit()
        }
    }
}