package com.example.hotelreviews.ui.fragments

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelreviews.R
import com.example.hotelreviews.model.Review
import com.squareup.picasso.Picasso

class ReviewsAdapter(
    private var reviews: List<Review>,
    private val showUserName: Boolean = true,
    private val showActions: Boolean = false,
    private val onEditClick: ((Review) -> Unit)? = null,
    private val onDeleteClick: ((Review) -> Unit)? = null,
    private val onUserClick: ((String, String) -> Unit)? = null
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageCard: View = view.findViewById(R.id.image_card)
        val imageView: ImageView = view.findViewById(R.id.review_image)
        val hotelNameText: TextView = view.findViewById(R.id.hotel_name_text)
        val hotelCityText: TextView = view.findViewById(R.id.hotel_city_text)
        val apiRatingContainer: View = view.findViewById(R.id.api_rating_container)
        val apiRatingText: TextView = view.findViewById(R.id.api_rating_text)
        val userNameText: TextView = view.findViewById(R.id.user_name_text)
        val userProfileCard: View = view.findViewById(R.id.user_profile_card)
        val userProfileImage: ImageView = view.findViewById(R.id.user_profile_image)
        val hotelRatingText: TextView = view.findViewById(R.id.hotel_rating_text)
        val descriptionText: TextView = view.findViewById(R.id.review_description_text)
        val feedFooterLayout: View = view.findViewById(R.id.feed_footer_layout)
        val timeText: TextView = view.findViewById(R.id.time_text)
        val actionsLayout: View = view.findViewById(R.id.actions_layout)
        val editButton: View = view.findViewById(R.id.edit_button)
        val deleteButton: View = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_list_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.hotelNameText.text = review.hotelName
        holder.hotelCityText.text = if (review.address.isNotEmpty()) review.address else review.city
        holder.hotelRatingText.text = review.rating.toInt().toString()
        holder.descriptionText.text = review.description
        
        // Google Places API Rating
        if (review.apiRating > 0) {
            holder.apiRatingContainer.visibility = View.VISIBLE
            holder.apiRatingText.text = String.format("%.1f (%d)", review.apiRating, review.apiReviewCount)
        } else {
            holder.apiRatingContainer.visibility = View.GONE
        }

        // Relative time logic
        val now = System.currentTimeMillis()
        val diff = now - review.timestamp
        if (diff < 60000) {
            holder.timeText.text = "Just now"
        } else {
            val relativeTime = DateUtils.getRelativeTimeSpanString(
                review.timestamp, 
                now, 
                DateUtils.MINUTE_IN_MILLIS
            )
            holder.timeText.text = relativeTime
        }

        // Image Handling and "More Little High" logic
        if (review.imageUrl.isNotEmpty()) {
            holder.imageCard.visibility = View.VISIBLE
            // Reset padding if recycled from a non-image item
            holder.hotelNameText.setPadding(0, 0, 0, 0)
            Picasso.get()
                .load(review.imageUrl)
                .fit()
                .centerCrop()
                .stableKey(review.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imageView)
        } else {
            holder.imageCard.visibility = View.GONE
            // Increase top padding for reviews without images to make them look better (taller)
            holder.hotelNameText.setPadding(0, 50, 0, 0)
        }

        // User Details handling
        if (showUserName) {
            holder.feedFooterLayout.visibility = View.VISIBLE
            
            val displayUserName = if (review.userName.isNullOrBlank()) "Anonymous" else review.userName
            holder.userNameText.text = displayUserName
            
            val onProfileClick = View.OnClickListener {
                if (!review.userId.isNullOrBlank()) {
                    onUserClick?.invoke(review.userId, displayUserName)
                }
            }
            
            holder.userNameText.setOnClickListener(onProfileClick)
            holder.userProfileCard.setOnClickListener(onProfileClick)

            if (!review.userProfileImageUrl.isNullOrBlank()) {
                Picasso.get()
                    .load(review.userProfileImageUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .fit()
                    .centerCrop()
                    .into(holder.userProfileImage)
            } else {
                holder.userProfileImage.setImageResource(R.drawable.ic_person)
            }
        } else {
            holder.feedFooterLayout.visibility = View.GONE
        }
        
        // Actions handling
        if (showActions) {
            holder.actionsLayout.visibility = View.VISIBLE
            holder.editButton.setOnClickListener { onEditClick?.invoke(review) }
            holder.deleteButton.setOnClickListener { onDeleteClick?.invoke(review) }
        } else {
            holder.actionsLayout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
