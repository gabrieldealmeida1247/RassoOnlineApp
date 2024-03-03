package com.example.rassoonlineapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.rassoonlineapp.Fragments.HomeFragment
import com.example.rassoonlineapp.Fragments.MenuFragment
import com.example.rassoonlineapp.Fragments.NotificationsFragment
import com.example.rassoonlineapp.Fragments.ProfileFragment
import com.example.rassoonlineapp.Fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener


class MainActivity : AppCompatActivity() {



    private val onNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                moveToFragment(HomeFragment())
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_menu -> {
                moveToFragment(MenuFragment())
                return@OnNavigationItemSelectedListener true


            }
            R.id.nav_add_post -> {
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_notifications -> {
                moveToFragment(NotificationsFragment())
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_profile -> {
                moveToFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true

            }


        }

        false
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        moveToFragment(HomeFragment())


    }
    //transião dos fragmentes quando clicado no botao de navegação
  /*
    private fun moveToFragment(fragment: Fragment){

        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()
    }
*/

    fun navigateToSearchFragment() {
        moveToFragment(SearchFragment())
    }

    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.addToBackStack(null)  // Add to back stack to enable back navigation
        fragmentTrans.commit()
    }
}