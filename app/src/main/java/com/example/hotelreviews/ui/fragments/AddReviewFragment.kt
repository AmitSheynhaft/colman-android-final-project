package com.example.hotelreviews.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hotelreviews.R
import com.example.hotelreviews.model.Review
import com.example.hotelreviews.model.ReviewModel
import com.example.hotelreviews.viewmodel.ReviewViewModel
import com.squareup.picasso.Picasso
import java.util.UUID

class AddReviewFragment : Fragment() {

    private val viewModel: ReviewViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                selectedImageBitmap = bitmap
                val imageView = view?.findViewById<ImageView>(R.id.review_image_view)
                imageView?.setImageBitmap(bitmap)
            }
        }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val imageView = view?.findViewById<ImageView>(R.id.review_image_view)
                Picasso.get().load(it).into(imageView)
                // In a real app we'd convert Uri to Bitmap if needed for upload
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hotelNameEditText = view.findViewById<EditText>(R.id.hotel_name_edit_text)
        val descriptionEditText = view.findViewById<EditText>(R.id.description_edit_text)
        val captureButton = view.findViewById<Button>(R.id.capture_button)
        val galleryButton = view.findViewById<Button>(R.id.gallery_button)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        val progressBar = view.findViewById<ProgressBar>(R.id.save_progress_bar)

        captureButton.setOnClickListener { cameraLauncher.launch(null) }
        galleryButton.setOnClickListener { galleryLauncher.launch("image/*") }

        saveButton.setOnClickListener {
            val name = hotelNameEditText.text.toString()
            val desc = descriptionEditText.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Hotel name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageBitmap != null) {
                uploadImageAndSaveReview(name, desc, selectedImageBitmap!!)
            } else {
                saveReview(name, desc, "")
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            saveButton.isEnabled = !isLoading
        }
    }

    private fun uploadImageAndSaveReview(name: String, desc: String, bitmap: Bitmap) {
        val fileName = UUID.randomUUID().toString()
        viewModel.isLoading // Just to be safe, but we'll manage progress locally or via VM
        
        ReviewModel.uploadImage(bitmap, fileName) { imageUrl ->
            if (imageUrl != null) {
                saveReview(name, desc, imageUrl)
            } else {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                saveReview(name, desc, "")
            }
        }
    }

    private fun saveReview(name: String, desc: String, imageUrl: String) {
        val review = Review(
            hotelName = name,
            description = desc,
            imageUrl = imageUrl,
            city = "", // Dummy for now
            rating = 5.0, // Dummy
            placeId = "",
            apiRating = 0.0,
            apiReviewCount = 0
        )
        viewModel.addReview(review) {
            findNavController().navigateUp()
        }
    }
}
