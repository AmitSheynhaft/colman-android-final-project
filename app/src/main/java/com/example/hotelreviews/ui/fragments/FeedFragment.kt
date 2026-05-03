package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelreviews.R
import com.example.hotelreviews.model.Review
import com.example.hotelreviews.viewmodel.AuthViewModel
import com.example.hotelreviews.viewmodel.ReviewViewModel
import com.google.android.material.button.MaterialButton

class FeedFragment : Fragment() {

    private val viewModel: ReviewViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var adapter: ReviewsAdapter
    
    private var allReviewsList: List<Review> = emptyList()
    private var searchQuery: String = ""
    private var isSortNewest: Boolean = true

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
        val searchEditText = view.findViewById<EditText>(R.id.search_edit_text)
        val sortNewestButton = view.findViewById<MaterialButton>(R.id.sort_newest_button)
        val sortOldestButton = view.findViewById<MaterialButton>(R.id.sort_oldest_button)
        
        adapter = ReviewsAdapter(
            reviews = emptyList(),
            onUserClick = { userId, userName ->
                val bundle = Bundle().apply {
                    putString("userId", userId)
                    putString("userName", userName)
                }
                findNavController().navigate(R.id.action_feedFragment_to_userReviewsFragment, bundle)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        viewModel.allReviews.observe(viewLifecycleOwner) { reviews ->
            allReviewsList = reviews
            applyFiltersAndSort()
            subtitleText.text = getString(R.string.reviews_shared_count, reviews.size)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.lowercase() ?: ""
                applyFiltersAndSort()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        sortNewestButton.setOnClickListener {
            isSortNewest = true
            updateSortButtons(sortNewestButton, sortOldestButton)
            applyFiltersAndSort()
        }

        sortOldestButton.setOnClickListener {
            isSortNewest = false
            updateSortButtons(sortNewestButton, sortOldestButton)
            applyFiltersAndSort()
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
        }
    }

    private fun applyFiltersAndSort() {
        var filteredList = if (searchQuery.isEmpty()) {
            allReviewsList
        } else {
            allReviewsList.filter { 
                it.hotelName.lowercase().contains(searchQuery) || 
                it.city.lowercase().contains(searchQuery) ||
                it.address.lowercase().contains(searchQuery)
            }
        }

        filteredList = if (isSortNewest) {
            filteredList.sortedByDescending { it.timestamp }
        } else {
            filteredList.sortedBy { it.timestamp }
        }

        adapter.updateReviews(filteredList)
    }

    private fun updateSortButtons(newestBtn: MaterialButton, oldestBtn: MaterialButton) {
        if (isSortNewest) {
            newestBtn.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.purple_primary)
            newestBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary))
            oldestBtn.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.gray_text)
            oldestBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
        } else {
            oldestBtn.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.purple_primary)
            oldestBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary))
            newestBtn.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.gray_text)
            newestBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshReviews()
    }
}
