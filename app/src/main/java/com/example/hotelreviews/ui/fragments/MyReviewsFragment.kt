package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelreviews.R
import com.example.hotelreviews.model.AuthModel
import com.example.hotelreviews.viewmodel.AuthViewModel
import com.example.hotelreviews.viewmodel.ReviewViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyReviewsFragment : Fragment() {

    private val viewModel: ReviewViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var adapter: ReviewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val subtitleText = view.findViewById<TextView>(R.id.subtitle_text)
        val logoutButton = view.findViewById<View>(R.id.logout_button)
        val recyclerView = view.findViewById<RecyclerView>(R.id.reviews_recycler_view)
        val emptyStateLayout = view.findViewById<View>(R.id.empty_state_layout)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        val addFab = view.findViewById<FloatingActionButton>(R.id.add_review_fab)
        val addFirstReviewButton = view.findViewById<Button>(R.id.add_first_review_button)
        
        adapter = ReviewsAdapter(
            reviews = emptyList(),
            showUserName = false,
            showActions = true,
            onEditClick = { review ->
                val bundle = Bundle().apply {
                    putString("reviewId", review.id)
                }
                findNavController().navigate(R.id.action_myReviewsFragment_to_addReviewFragment, bundle)
            },
            onDeleteClick = { review ->
                // Basic confirmation could be added here, but for now direct delete
                viewModel.deleteReview(review) {
                    Toast.makeText(requireContext(), "Review deleted", Toast.LENGTH_SHORT).show()
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        val currentUserId = AuthModel.getCurrentUser()?.uid ?: ""
        viewModel.getReviewsByUserId(currentUserId).observe(viewLifecycleOwner) { reviews ->
            adapter.updateReviews(reviews)
            
            val count = reviews.size
            subtitleText.text = if (count == 1) "1 review" else "$count reviews"
            
            if (reviews.isEmpty()) {
                emptyStateLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                addFab.visibility = View.GONE
            } else {
                emptyStateLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                addFab.visibility = View.VISIBLE
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefresh.isRefreshing = isLoading
        }
        
        swipeRefresh.setOnRefreshListener {
            viewModel.refreshReviews()
        }

        val onAddClick = View.OnClickListener {
            findNavController().navigate(R.id.action_myReviewsFragment_to_addReviewFragment)
        }
        
        addFab.setOnClickListener(onAddClick)
        addFirstReviewButton.setOnClickListener(onAddClick)

        logoutButton.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshReviews()
    }
}
