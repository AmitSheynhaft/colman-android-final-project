package com.example.hotelreviews

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.hotelreviews.databinding.ActivityMainBinding
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCVYl7LNAR3PkHgTCyZzTciWSEKINV2TlA")
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        val bottomNav = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)

        // Observe auth state globally to handle logout/session expiration
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                runOnUiThread {
                    try {
                        val currentDest = navController.currentDestination?.id
                        if (currentDest != null && currentDest != R.id.loginFragment && currentDest != R.id.registerFragment) {
                            navController.navigate(R.id.action_global_loginFragment)
                        }
                    } catch (e: Exception) {
                        // Navigation might fail if already in progress
                    }
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.registerFragment) {
                bottomNav.visibility = View.GONE
            } else {
                bottomNav.visibility = View.VISIBLE
            }
        }
    }
}
