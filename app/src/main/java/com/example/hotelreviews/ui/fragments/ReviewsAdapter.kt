package com.example.hotelreviews.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelreviews.R
import com.example.hotelreviews.model.Review
import com.squareup.picasso.Picasso

class ReviewsAdapter(private var reviews: List<Review>) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.review_image)
        val imageCard: View = view.findViewById(R.id.image_card)
        val hotelNameText: TextView = view.findViewById(R.id.hotel_name_text)
        val hotelCityText: TextView = view.findViewById(R.id.hotel_city_text)
        val hotelRatingBar: RatingBar = view.findViewById(R.id.hotel_rating_bar)
        val descriptionText: TextView = view.findViewById(R.id.review_description_text)
        val apiRatingBadge: View = view.findViewById(R.id.api_rating_badge)
        val apiRatingText: TextView = view.findViewById(R.id.api_rating_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_list_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.hotelNameText.text = review.hotelName
        holder.hotelCityText.text = if (review.address.isNotEmpty()) review.address else review.city
        holder.hotelRatingBar.rating = review.rating.toFloat()
        holder.descriptionText.text = review.description
        
        if (review.apiRating > 0) {
            holder.apiRatingBadge.visibility = View.VISIBLE
            holder.apiRatingText.text = String.format("%.1f (%d)", review.apiRating, review.apiReviewCount)
        } else {
            holder.apiRatingBadge.visibility = View.GONE
        }

        if (review.imageUrl.isNotEmpty()) {
            holder.imageCard.visibility = View.VISIBLE
            Picasso.get()
                .load(review.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imageView)
        } else {
            holder.imageCard.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
