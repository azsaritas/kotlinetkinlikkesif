package com.azi.etkinlikkesifkotlin1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.azi.etkinlikkesifkotlin1.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        // Bottom Navigation'ı NavController ile ilişkilendir
        binding.bottomNavigation.setupWithNavController(navController)

        binding.bottomNavigation.visibility = View.GONE


        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.etkinlik -> navController.navigate(R.id.etkinlikFragment)
                R.id.harita -> navController.navigate(R.id.mapFragment)
                R.id.profil -> navController.navigate(R.id.profileFragment)
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment).commit()
    }
    fun onLoginSuccess() {
        // Bottom Navigation'ı görünür yap
        binding.bottomNavigation.visibility = View.VISIBLE

        // EtkinlikFragment'a geç
        navController.navigate(R.id.etkinlikFragment)
    }
}

