package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hotelreviews.R
import com.example.hotelreviews.databinding.FragmentMyReviewsBinding
import com.example.hotelreviews.ui.adapters.ReviewAdapter
import com.example.hotelreviews.viewmodel.ReviewViewModel

class MyReviewsFragment : Fragment() {

    private var _binding: FragmentMyReviewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewViewModel by viewModels()
    private lateinit var adapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReviewAdapter(
            isMyReviews = true,
            onReviewClick = { review ->
                // Navigate to details if needed
            },
            onEditClick = { review ->
                // Handle edit
            },
            onDeleteClick = { review ->
                // Handle delete
            }
        )
        binding.reviewsRecyclerView.adapter = adapter

        viewModel.userReviews.observe(viewLifecycleOwner) { reviews ->
            adapter.submitList(reviews)
            val isEmpty = reviews.isEmpty()
            binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.reviewsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.tvReviewCount.text = "${reviews.size} reviews"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        binding.btnLogout.setOnClickListener {
            // authViewModel.logout() if available, or navigate to login
            findNavController().navigate(R.id.action_myReviewsFragment_to_loginFragment)
        }

        binding.btnAddFirst.setOnClickListener {
            findNavController().navigate(R.id.action_myReviewsFragment_to_addReviewFragment)
        }

        viewModel.refreshReviews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
