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
import com.example.hotelreviews.databinding.FragmentMyReviewsBinding
import com.example.hotelreviews.ui.screens.MyReviewsScreen
import com.example.hotelreviews.ui.theme.HotelReviewsTheme
import com.example.hotelreviews.viewmodel.AuthViewModel
import com.example.hotelreviews.viewmodel.ReviewViewModel

class MyReviewsFragment : Fragment() {

    private var _binding: FragmentMyReviewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReviewsBinding.inflate(inflater, container, false)
        
        binding.composeView.setContent {
            HotelReviewsTheme {
                val reviews by viewModel.allReviews.observeAsState(initial = emptyList())
                val isLoading by viewModel.isLoading.observeAsState(initial = false)
                
                MyReviewsScreen(
                    reviews = reviews,
                    isLoading = isLoading,
                    onAddReviewClick = {
                        findNavController().navigate(R.id.action_myReviewsFragment_to_addReviewFragment)
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                        findNavController().navigate(R.id.action_myReviewsFragment_to_loginFragment)
                    }
                )
            }
        }
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.refreshReviews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
