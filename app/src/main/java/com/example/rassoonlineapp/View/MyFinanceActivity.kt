package com.example.rassoonlineapp.View

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.rassoonlineapp.Fragments.AccountMovementFragment
import com.example.rassoonlineapp.Fragments.CreditCardsFragment
import com.example.rassoonlineapp.Fragments.FinancialReportsFragment
import com.example.rassoonlineapp.Fragments.PaymentFragment
import com.example.rassoonlineapp.Fragments.ReceiptFragment
import com.example.rassoonlineapp.R
import com.google.android.material.navigation.NavigationView

@Suppress("DEPRECATION")
class MyFinanceActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_finance)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PaymentFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AccountMovementFragment()).commit()

            R.id.nav_credit -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreditCardsFragment()).commit()

            R.id.nav_addCredit -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreditCardsFragment()).commit()


            R.id.nav_trans -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PaymentFragment()).commit()


            R.id.nav_rec -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReceiptFragment()).commit()

            R.id.nav_about -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FinancialReportsFragment()).commit()

        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}