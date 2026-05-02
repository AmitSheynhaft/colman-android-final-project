package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hotelreviews.databinding.FragmentGlobalFeedBinding
import com.example.hotelreviews.ui.adapters.ReviewAdapter
import com.example.hotelreviews.viewmodel.ReviewsViewModel

class GlobalFeedFragment : Fragment() {

    private var _binding: FragmentGlobalFeedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewsViewModel by viewModels()
    private lateinit var adapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGlobalFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReviewAdapter { review ->
            // Handle review click if needed
        }
        binding.feedRecyclerView.adapter = adapter

        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            adapter.submitList(reviews)
        }

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
