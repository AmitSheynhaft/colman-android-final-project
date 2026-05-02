package com.example.hotelreviews.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelreviews.R
import com.example.hotelreviews.databinding.ItemReviewBinding
import com.example.hotelreviews.model.Review
import com.squareup.picasso.Picasso

class ReviewAdapter(
    private val isMyReviews: Boolean = false,
    private val onReviewClick: (Review) -> Unit,
    private val onEditClick: ((Review) -> Unit)? = null,
    private val onDeleteClick: ((Review) -> Unit)? = null
) : ListAdapter<Review, ReviewAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding, isMyReviews, onReviewClick, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewViewHolder(
        private val binding: ItemReviewBinding,
        private val isMyReviews: Boolean,
        private val onReviewClick: (Review) -> Unit,
        private val onEditClick: ((Review) -> Unit)?,
        private val onDeleteClick: ((Review) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.hotelName.text = review.hotelName
            binding.reviewDescription.text = review.description
            binding.tvRating.text = review.rating.toString()
            
            // Note: Location is not in the model yet, using placeholder as in Figma
            binding.hotelLocation.text = "Tel Aviv" 

            if (isMyReviews) {
                binding.layoutActions.visibility = View.VISIBLE
                binding.layoutAuthor.visibility = View.GONE
                
                binding.btnEdit.setOnClickListener { onEditClick?.invoke(review) }
                binding.btnDelete.setOnClickListener { onDeleteClick?.invoke(review) }
            } else {
                binding.layoutActions.visibility = View.GONE
                binding.layoutAuthor.visibility = View.VISIBLE
                
                // Author placeholders as per Figma
                binding.authorName.text = "Amit Sheynhaft"
                binding.tvTime.text = "Just now"
            }

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
