package com.example.hotelreviews.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelreviews.R
import com.example.hotelreviews.model.Review
import com.squareup.picasso.Picasso

class ReviewsAdapter(private var reviews: List<Review>) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.review_image)
        val hotelNameText: TextView = view.findViewById(R.id.hotel_name_text)
        val descriptionText: TextView = view.findViewById(R.id.description_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_list_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.hotelNameText.text = review.hotelName
        holder.descriptionText.text = review.description
        
        if (review.imageUrl.isNotEmpty()) {
            Picasso.get().load(review.imageUrl).into(holder.imageView)
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
