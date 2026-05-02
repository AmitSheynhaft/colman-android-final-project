package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hotelreviews.databinding.FragmentAddReviewBinding
import com.example.hotelreviews.model.Review
import com.example.hotelreviews.viewmodel.ReviewViewModel

class AddReviewFragment : Fragment() {

    private var _binding: FragmentAddReviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveReview.isEnabled = !isLoading
        }

        binding.btnSaveReview.setOnClickListener {
            val hotelName = binding.editHotelName.text.toString()
            val rating = binding.ratingBar.rating.toInt()
            val description = binding.editDescription.text.toString()
            val imageUrl = binding.editImageUrl.text.toString()

            if (hotelName.isNotBlank() && description.isNotBlank()) {
                val review = Review(
                    hotelName = hotelName,
                    rating = rating,
                    description = description,
                    imageUrl = imageUrl
                )
                viewModel.addReview(review) {
                    findNavController().navigateUp()
                }
            } else {
                Toast.makeText(context, "Please fill in hotel name and description", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
