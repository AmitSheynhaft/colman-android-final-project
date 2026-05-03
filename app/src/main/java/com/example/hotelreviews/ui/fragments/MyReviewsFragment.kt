package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.hotelreviews.R
import com.example.hotelreviews.model.AuthModel
import com.example.hotelreviews.viewmodel.AuthViewModel
import com.example.hotelreviews.viewmodel.ReviewViewModel
import com.example.hotelreviews.viewmodel.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso

class MyReviewsFragment : Fragment() {

    private val viewModel: ReviewViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var adapter: ReviewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        
        val recyclerView = view.findViewById<RecyclerView>(R.id.reviews_recycler_view)
        val progressBar = view.findViewById<ProgressBar>(R.id.reviews_progress_bar)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        
        adapter = ReviewsAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        val currentUserId = AuthModel.getCurrentUser()?.uid ?: ""
        viewModel.getReviewsByUserId(currentUserId).observe(viewLifecycleOwner) { reviews ->
            adapter.updateReviews(reviews)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            swipeRefresh.isRefreshing = isLoading
        }
        
        swipeRefresh.setOnRefreshListener {
            viewModel.refreshReviews()
        }

        val addFab = view.findViewById<FloatingActionButton>(R.id.add_review_fab)
        addFab.setOnClickListener {
            findNavController().navigate(R.id.action_myReviewsFragment_to_addReviewFragment)
        }

        val headerName = view.findViewById<TextView>(R.id.header_profile_name)
        val headerImage = view.findViewById<ImageView>(R.id.header_profile_image)
        val editProfileBtn = view.findViewById<View>(R.id.edit_profile_button)

        userViewModel.getCurrentUser()?.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                headerName.text = user.name
                if (user.profileImageUrl.isNotEmpty()) {
                    Picasso.get().load(user.profileImageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(headerImage)
                }
            }
        }
        userViewModel.refreshCurrentUser()

        editProfileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_myReviewsFragment_to_editProfileFragment)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.my_reviews_menu, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                authViewModel.logout()
                findNavController().navigate(R.id.action_myReviewsFragment_to_loginFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshReviews()
        userViewModel.refreshCurrentUser()
    }
}
