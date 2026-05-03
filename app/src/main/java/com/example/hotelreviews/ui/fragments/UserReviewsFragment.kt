package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelreviews.R
import com.example.hotelreviews.viewmodel.ReviewViewModel

class UserReviewsFragment : Fragment() {

    private val viewModel: ReviewViewModel by viewModels()
    private lateinit var adapter: ReviewsAdapter
    private var userId: String = ""
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("userId") ?: ""
        userName = arguments?.getString("userName") ?: "User"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val titleText = view.findViewById<TextView>(R.id.title_text)
        val subtitleText = view.findViewById<TextView>(R.id.subtitle_text)
        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        val recyclerView = view.findViewById<RecyclerView>(R.id.reviews_recycler_view)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        
        titleText.text = if (userName.endsWith("s")) "$userName' Reviews" else "$userName's Reviews"
        subtitleText.text = "Browsing all shared experiences"
        
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        adapter = ReviewsAdapter(emptyList(), showUserName = false)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        viewModel.getReviewsByUserId(userId).observe(viewLifecycleOwner) { reviews ->
            adapter.updateReviews(reviews)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            swipeRefresh.isRefreshing = isLoading
        }
        
        swipeRefresh.setOnRefreshListener {
            viewModel.refreshReviews()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshReviews()
    }
}
