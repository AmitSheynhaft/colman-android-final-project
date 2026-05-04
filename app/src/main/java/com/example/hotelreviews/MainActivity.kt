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
                binding.root.post {
                    try {
                        val currentDestId = navController.currentDestination?.id
                        if (currentDestId != null && currentDestId != R.id.loginFragment && currentDestId != R.id.registerFragment) {
                            // Standard way to reset to start destination: navigate to it and pop everything inclusive of start
                            navController.navigate(R.id.loginFragment) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    } catch (e: Exception) {
                        // Fallback: reset the graph completely
                        navController.setGraph(R.id.nav_graph)
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
