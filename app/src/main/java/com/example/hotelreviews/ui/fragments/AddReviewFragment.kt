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
import com.example.hotelreviews.databinding.FragmentComposeHostBinding
import com.example.hotelreviews.model.Review
import com.example.hotelreviews.ui.screens.AddReviewScreen
import com.example.hotelreviews.ui.theme.HotelReviewsTheme
import com.example.hotelreviews.viewmodel.ReviewViewModel

class AddReviewFragment : Fragment() {

    private var _binding: FragmentComposeHostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeHostBinding.inflate(inflater, container, false)
        
        binding.composeView.setContent {
            HotelReviewsTheme {
                val isLoading by viewModel.isLoading.observeAsState(initial = false)

                AddReviewScreen(
                    isLoading = isLoading,
                    onBackClick = { findNavController().navigateUp() },
                    onSaveClick = { name, city, rating, desc, url, placeId, apiRating, apiReviewCount ->
                        val review = Review(
                            hotelName = name,
                            city = city,
                            rating = rating.toDouble(),
                            description = desc,
                            imageUrl = url,
                            placeId = placeId,
                            apiRating = apiRating,
                            apiReviewCount = apiReviewCount
                        )
                        viewModel.addReview(review) {
                            findNavController().navigateUp()
                        }
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
