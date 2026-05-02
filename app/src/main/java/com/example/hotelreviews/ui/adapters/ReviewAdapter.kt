package com.example.hotelreviews.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelreviews.R
import com.example.hotelreviews.databinding.ItemReviewBinding
import com.example.hotelreviews.model.Review
import com.squareup.picasso.Picasso

class ReviewAdapter(private val onReviewClick: (Review) -> Unit) :
    ListAdapter<Review, ReviewAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding, onReviewClick)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewViewHolder(
        private val binding: ItemReviewBinding,
        private val onReviewClick: (Review) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.hotelName.text = review.hotelName
            binding.reviewDescription.text = review.description
            binding.ratingBar.rating = review.rating.toFloat()

            if (review.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(review.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.reviewImage)
            } else {
                binding.reviewImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            binding.root.setOnClickListener { onReviewClick(review) }
        }
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem == newItem
        }
    }
}
