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
import androidx.navigation.fragment.findNavController
import com.example.hotelreviews.R
import com.example.hotelreviews.model.Review
import com.example.hotelreviews.viewmodel.AuthViewModel
import com.example.hotelreviews.viewmodel.ReviewViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.squareup.picasso.Picasso

class AddReviewFragment : Fragment() {

    private val viewModel: ReviewViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
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
                
                requireContext().contentResolver.openInputStream(it)?.use { stream ->
                    selectedImageBitmap = BitmapFactory.decodeStream(stream)
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
        val captureButton = view.findViewById<Button>(R.id.capture_button)
        val galleryButton = view.findViewById<Button>(R.id.gallery_button)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        val progressBar = view.findViewById<ProgressBar>(R.id.save_progress_bar)
        val ratingBar = view.findViewById<RatingBar>(R.id.rating_bar)
        val logoutButton = view.findViewById<View>(R.id.logout_button)

        // Initialize UI with default value
        ratingBar.rating = userRating
        ratingText.text = getString(R.string.x_out_of_5_stars, userRating.toInt().toString())

        setupPlacesAutocomplete(hotelNameEditText, cityEditText)

        logoutButton.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.loginFragment) {
                popUpTo(R.id.nav_graph) { inclusive = true }
            }
        }

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            userRating = rating
            ratingText.text = getString(R.string.x_out_of_5_stars, rating.toInt().toString())
        }

        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                characterCountText.text = getString(R.string.characters_count, s?.length ?: 0)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        captureButton.setOnClickListener { cameraLauncher.launch(null) }
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

            val review = Review(
                hotelName = name,
                address = selectedHotelAddress.ifEmpty { city },
                description = desc,
                city = city,
                rating = userRating.toDouble(),
                placeId = selectedPlaceId,
                apiRating = selectedApiRating,
                apiReviewCount = selectedApiReviewCount
            )

            viewModel.uploadImageAndAddReview(review, selectedImageBitmap) {
                findNavController().navigateUp()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            saveButton.isEnabled = !isLoading
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

        autocompleteFragment.setHint(getString(R.string.search_hotel_hint))
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
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(requireContext(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
