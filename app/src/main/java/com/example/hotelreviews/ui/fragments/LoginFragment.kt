package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hotelreviews.R
import com.example.hotelreviews.databinding.FragmentComposeHostBinding
import com.example.hotelreviews.ui.screens.LoginScreen
import com.example.hotelreviews.ui.theme.HotelReviewsTheme
import com.example.hotelreviews.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentComposeHostBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeHostBinding.inflate(inflater, container, false)
        
        binding.composeView.setContent {
            HotelReviewsTheme {
                val isLoading by authViewModel.isLoading.observeAsState(initial = false)
                val errorMessage by authViewModel.errorMessage.observeAsState()

                LoginScreen(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onLoginClick = { email, password ->
                        authViewModel.login(email, password) {
                            findNavController().navigate(R.id.action_loginFragment_to_myReviewsFragment)
                        }
                    },
                    onRegisterClick = {
                        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                    },
                    onDismissError = {
                        authViewModel.clearError()
                    }
                )
            }
        }
        
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
