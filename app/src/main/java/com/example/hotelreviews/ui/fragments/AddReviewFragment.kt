package com.example.hotelreviews.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.hotelreviews.R
import com.example.hotelreviews.model.Review
import com.example.hotelreviews.viewmodel.AuthViewModel
import com.example.hotelreviews.viewmodel.ReviewViewModel
import com.example.hotelreviews.viewmodel.UserViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.squareup.picasso.Picasso

class AddReviewFragment : Fragment() {

    private val viewModel: ReviewViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    
    private var selectedHotelName: String = ""
    private var selectedHotelCity: String = ""
    private var selectedHotelAddress: String = ""
    private var selectedPlaceId: String = ""
    private var selectedApiRating: Double = 0.0
    private var selectedApiReviewCount: Int = 0
    private var userRating: Float = 3.0f
    private var currentUserName: String = ""
    private var currentUserProfileImageUrl: String = ""
    private var existingReviewId: String? = null
    private var existingImageUrl: String = ""
    private var isImageDeleted: Boolean = false
    
    // Initial values for change tracking
    private var initialHotelName = ""
    private var initialCity = ""
    private var initialDescription = ""
    private var initialRating = 3.0f
    private var initialImageUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        existingReviewId = arguments?.getString("reviewId")
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val imageView = view?.findViewById<ImageView>(R.id.review_image_view)
                Picasso.get().load(it).into(imageView)
                
                // Optimized decoding: Decode to a reasonable size first to save time/memory
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                requireContext().contentResolver.openInputStream(it)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
                
                // Target a max of 1024px for the initial bitmap to speed up processing
                var inSampleSize = 1
                val maxDim = 1024
                if (options.outHeight > maxDim || options.outWidth > maxDim) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while (halfHeight / inSampleSize >= maxDim && halfWidth / inSampleSize >= maxDim) {
                        inSampleSize *= 2
                    }
                }
                
                val finalOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }
                requireContext().contentResolver.openInputStream(it)?.use { stream ->
                    selectedImageBitmap = BitmapFactory.decodeStream(stream, null, finalOptions)
                }
                
                // Show delete button when image is selected
                view?.findViewById<Button>(R.id.delete_photo_button)?.visibility = View.VISIBLE
                isImageDeleted = false
                
                // Trigger change check
                val saveButton = view?.findViewById<Button>(R.id.save_button)
                val hotelNameEditText = view?.findViewById<EditText>(R.id.hotel_name_edit_text)
                val cityEditText = view?.findViewById<EditText>(R.id.city_edit_text)
                val descriptionEditText = view?.findViewById<EditText>(R.id.description_edit_text)
                val ratingBar = view?.findViewById<RatingBar>(R.id.rating_bar)
                
                if (saveButton != null && hotelNameEditText != null && cityEditText != null && 
                    descriptionEditText != null && ratingBar != null) {
                    
                    val name = hotelNameEditText.text.toString().trim()
                    val city = cityEditText.text.toString().trim()
                    val desc = descriptionEditText.text.toString().trim()
                    val rating = ratingBar.rating

                    val hasChanged = name != initialHotelName ||
                            city != initialCity ||
                            desc != initialDescription ||
                            rating != initialRating ||
                            selectedImageBitmap != null ||
                            isImageDeleted

                    saveButton.isEnabled = hasChanged && name.isNotEmpty() && city.isNotEmpty()
                    saveButton.alpha = if (saveButton.isEnabled) 1.0f else 0.5f
                }
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
        val cityEditText = view.findViewById<EditText>(R.id.city_edit_text)
        val descriptionEditText = view.findViewById<EditText>(R.id.description_edit_text)
        val ratingText = view.findViewById<TextView>(R.id.rating_text)
        val characterCountText = view.findViewById<TextView>(R.id.character_count_text)
        val galleryButton = view.findViewById<Button>(R.id.gallery_button)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        val progressBar = view.findViewById<ProgressBar>(R.id.save_progress_bar)
        val ratingBar = view.findViewById<RatingBar>(R.id.rating_bar)
        val logoutButton = view.findViewById<View>(R.id.logout_button)
        val imageView = view.findViewById<ImageView>(R.id.review_image_view)
        val deletePhotoButton = view.findViewById<Button>(R.id.delete_photo_button)

        fun checkIfChanged() {
            val name = hotelNameEditText.text.toString().trim()
            val city = cityEditText.text.toString().trim()
            val desc = descriptionEditText.text.toString().trim()
            val rating = ratingBar.rating

            val hasChanged = name != initialHotelName ||
                    city != initialCity ||
                    desc != initialDescription ||
                    rating != initialRating ||
                    selectedImageBitmap != null ||
                    isImageDeleted

            saveButton.isEnabled = hasChanged && name.isNotEmpty() && city.isNotEmpty() && desc.isNotEmpty()
            saveButton.alpha = if (saveButton.isEnabled) 1.0f else 0.5f
        }

        // If editing, load existing review data
        existingReviewId?.let { id ->
            val observer = object : Observer<List<Review>> {
                override fun onChanged(value: List<Review>) {
                    value.find { it.id == id }?.let { review ->
                        initialHotelName = review.hotelName
                        initialCity = review.city
                        initialDescription = review.description
                        initialRating = review.rating.toFloat()
                        initialImageUrl = review.imageUrl
                        
                        hotelNameEditText.setText(review.hotelName)
                        cityEditText.setText(review.city)
                        descriptionEditText.setText(review.description)
                        ratingBar.rating = review.rating.toFloat()
                        userRating = review.rating.toFloat()
                        ratingText.text = getString(R.string.x_out_of_5_stars, userRating.toInt().toString())
                        
                        selectedHotelAddress = review.address
                        selectedPlaceId = review.placeId
                        selectedApiRating = review.apiRating
                        selectedApiReviewCount = review.apiReviewCount
                        existingImageUrl = review.imageUrl
                        
                        if (review.imageUrl.isNotEmpty()) {
                            Picasso.get().load(review.imageUrl).into(imageView)
                            deletePhotoButton.visibility = View.VISIBLE
                        }
                        
                        saveButton.text = "Update Review"
                        checkIfChanged()
                        
                        // Stop observing once we've loaded the data
                        viewModel.allReviews.removeObserver(this)
                    }
                }
            }
            viewModel.allReviews.observe(viewLifecycleOwner, observer)
        } ?: run {
            // New review, check initial state
            checkIfChanged()
        }

        // Listeners for change tracking
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkIfChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        hotelNameEditText.addTextChangedListener(textWatcher)
        cityEditText.addTextChangedListener(textWatcher)
        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                characterCountText.text = getString(R.string.characters_count, s?.length ?: 0)
                checkIfChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                userRating = rating
                ratingText.text = getString(R.string.x_out_of_5_stars, rating.toInt().toString())
                checkIfChanged()
            }
        }

        deletePhotoButton.setOnClickListener {
            selectedImageBitmap = null
            existingImageUrl = ""
            isImageDeleted = true
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            deletePhotoButton.visibility = View.GONE
            checkIfChanged()
        }

        // Initialize UI with default value
        ratingBar.rating = userRating
        ratingText.text = getString(R.string.x_out_of_5_stars, userRating.toInt().toString())

        setupPlacesAutocomplete(hotelNameEditText, cityEditText)

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUserName = user.name
                currentUserProfileImageUrl = user.profileImageUrl
            }
        }
        userViewModel.fetchUser()

        logoutButton.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_global_loginFragment)
        }

        galleryButton.setOnClickListener { galleryLauncher.launch("image/*") }

        saveButton.setOnClickListener {
            val name = hotelNameEditText.text.toString().trim()
            val city = cityEditText.text.toString().trim()
            val desc = descriptionEditText.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_name_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (city.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (desc.isEmpty()) {
                Toast.makeText(requireContext(), "Description cannot be empty or just spaces", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ensure user identity is captured from the latest ViewModel state
            val user = userViewModel.user.value
            val finalUserName = user?.name ?: currentUserName
            val finalProfileImage = user?.profileImageUrl ?: currentUserProfileImageUrl

            val review = Review(
                id = existingReviewId ?: "",
                hotelName = name,
                userName = finalUserName,
                userProfileImageUrl = finalProfileImage,
                address = selectedHotelAddress.ifEmpty { city },
                description = desc,
                city = city,
                rating = userRating.toDouble(),
                placeId = selectedPlaceId,
                apiRating = selectedApiRating,
                apiReviewCount = selectedApiReviewCount,
                imageUrl = existingImageUrl
            )

            viewModel.uploadImageAndAddReview(review, selectedImageBitmap) {
                findNavController().navigateUp()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            
            if (isLoading) {
                saveButton.isEnabled = false
                saveButton.text = if (existingReviewId != null) "Updating..." else "Submitting..."
            } else {
                saveButton.text = if (existingReviewId != null) "Update Review" else getString(R.string.submit_review)
                checkIfChanged()
            }
        }
    }

    private fun setupPlacesAutocomplete(hotelNameEditText: EditText, cityEditText: EditText) {
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(
            Place.Field.ID, 
            Place.Field.NAME,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.ADDRESS_COMPONENTS, 
            Place.Field.RATING, 
            Place.Field.USER_RATING_COUNT
        ))

        autocompleteFragment.setHint(getString(R.string.search_google_places))
        autocompleteFragment.setTypesFilter(listOf("establishment"))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                selectedHotelName = place.displayName ?: place.name ?: ""
                selectedPlaceId = place.id ?: ""
                selectedApiRating = place.rating ?: 0.0
                selectedApiReviewCount = place.userRatingCount ?: 0
                selectedHotelAddress = place.formattedAddress ?: ""
                
                hotelNameEditText.setText(selectedHotelName)
                
                var cityFound = false
                place.addressComponents?.asList()?.forEach { component ->
                    if (component.types.contains("locality")) {
                        selectedHotelCity = component.name
                        cityEditText.setText(selectedHotelCity)
                        cityFound = true
                    }
                }
                
                if (!cityFound && selectedHotelAddress.isNotEmpty()) {
                    // Fallback to extract city from formatted address if locality component is missing
                    val parts = selectedHotelAddress.split(",")
                    if (parts.size >= 2) {
                        selectedHotelCity = parts[parts.size - 2].trim()
                        cityEditText.setText(selectedHotelCity)
                    }
                }
                
                // Manual trigger of change check since we programmatically set text
                // However, TextWatcher should catch it. Let's be safe.
                // Since checkIfChanged is inside onViewCreated, we can't easily call it here.
                // But setupPlacesAutocomplete is also inside AddReviewFragment.
                // Wait, setupPlacesAutocomplete is a separate function.
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(requireContext(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
