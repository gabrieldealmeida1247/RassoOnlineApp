package com.example.rassoonlineapp.View

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rassoonlineapp.Fragments.HomeFragment
import com.example.rassoonlineapp.Fragments.MenuFragment
import com.example.rassoonlineapp.Fragments.NotificationsFragment
import com.example.rassoonlineapp.Fragments.ProfileFragment
import com.example.rassoonlineapp.Fragments.SearchFragment
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.ViewModel.WorkManager.SampleWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    // Dentro da classe MainActivity
    private lateinit var coroutineScope: CoroutineScope
    private val job = Job()
    private lateinit var auth: FirebaseAuth;



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
                item.isChecked = false
                startActivity(Intent(this@MainActivity, AddPostActivity::class.java))
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_notifications -> {
                moveToFragment(NotificationsFragment())
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_profile -> {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                val profileId = firebaseUser?.uid ?: ""

                // Salvar profileId nas SharedPreferences
                val pref = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("profileId", profileId)
                pref.apply()

                moveToFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true

            }

        }

        false

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "MainActivity onCreate")
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        moveToFragment(HomeFragment())

        // Inicialize o CoroutineScope
        coroutineScope = CoroutineScope(Dispatchers.Main + job)

        // Inicie o WorkManager
        startSampleWork()
    }
    //transião dos fragmentes quando clicado no botao de navegação
  /*
    private fun moveToFragment(fragment: Fragment){

        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()
    }
*/

    private fun startSampleWork() {
        val workRequest = OneTimeWorkRequestBuilder<SampleWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancela todas as coroutines quando a activity é destruída
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            // O usuário está autenticado
            Log.d("MainActivity", "Usuário autenticado: ${currentUser.uid}")
            // Aqui você pode atualizar a interface para mostrar conteúdo apropriado para usuários autenticados
        } else {
            // O usuário não está autenticado
            Log.d("MainActivity", "Usuário não autenticado.")
            // Aqui você pode atualizar a interface para mostrar conteúdo apropriado para usuários não autenticados
            // Por exemplo, você pode redirecionar para a tela de login ou mostrar uma mensagem para convidar o usuário a fazer login
        }
    }


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