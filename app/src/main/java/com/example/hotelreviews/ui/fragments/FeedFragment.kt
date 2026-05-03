package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelreviews.R
import com.example.hotelreviews.viewmodel.AuthViewModel
import com.example.hotelreviews.viewmodel.ReviewViewModel

class FeedFragment : Fragment() {

    private val viewModel: ReviewViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var adapter: ReviewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val subtitleText = view.findViewById<TextView>(R.id.subtitle_text)
        val logoutButton = view.findViewById<View>(R.id.logout_button)
        val recyclerView = view.findViewById<RecyclerView>(R.id.feed_recycler_view)
        val progressBar = view.findViewById<ProgressBar>(R.id.feed_progress_bar)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        
        adapter = ReviewsAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        viewModel.allReviews.observe(viewLifecycleOwner) { reviews ->
            adapter.updateReviews(reviews)
            subtitleText.text = getString(R.string.reviews_shared_count, reviews.size)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            swipeRefresh.isRefreshing = isLoading
        }
        
        swipeRefresh.setOnRefreshListener {
            viewModel.refreshReviews()
        }

        logoutButton.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.loginFragment) {
                popUpTo(R.id.nav_graph) { inclusive = true }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshReviews()
    }
}
