package com.example.boardgamegeekext

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.boardgamegeekext.api.RetrofitInstance
import com.example.boardgamegeekext.api.UserApi
import com.example.boardgamegeekext.database.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarItemView
import com.google.android.material.navigation.NavigationBarView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation : BottomNavigationView

    private val homeFragment = HomeFragment()
    private val settingsFragment = SettingsFragment()
    private val listFragment = ListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val intent = Intent(this, InitialConfiguration::class.java)
//        startActivity(intent)

//        val dbHandler = DatabaseHelper(this, null, null, 1)
//        val user = User("Marcin", "marcol13")
//
//        dbHandler.addUser(user)



        bottomNavigation = findViewById(R.id.bottom_navigation)

        setCurrentFragment(homeFragment)

        bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> setCurrentFragment(homeFragment)
                R.id.nav_settings -> setCurrentFragment(settingsFragment)
                R.id.nav_list -> setCurrentFragment(listFragment)
            }
            true
        })
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment)
            commit()
        }
    }
}