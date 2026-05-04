package com.example.hotelreviews.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hotelreviews.R
import com.example.hotelreviews.viewmodel.AuthViewModel
import com.example.hotelreviews.viewmodel.UserViewModel
import com.squareup.picasso.Picasso

class EditProfileFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var progressBar: ProgressBar
    
    private var initialName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Picasso.get().load(it).into(profileImageView)
                
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
                checkIfChanged()
            }
        }
    }

    private fun checkIfChanged() {
        val currentName = nameEditText.text.toString().trim()
        val hasChanged = currentName != initialName || selectedImageBitmap != null
        
        saveButton.isEnabled = hasChanged && currentName.isNotEmpty()
        saveButton.alpha = if (saveButton.isEnabled) 1.0f else 0.5f
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImageView = view.findViewById(R.id.profile_image_view)
        nameEditText = view.findViewById(R.id.name_edit_text)
        saveButton = view.findViewById(R.id.save_button)
        progressBar = view.findViewById(R.id.progress_bar)
        val selectImageButton = view.findViewById<View>(R.id.select_image_button)
        val logoutButton = view.findViewById<View>(R.id.logout_button)

        logoutButton.setOnClickListener {
            authViewModel.logout()
        }

        selectImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                initialName = user.name
                
                // Update display views
                view.findViewById<TextView>(R.id.display_name_large).text = user.name

                // Only update the name if the user hasn't started typing yet
                if (nameEditText.text.isEmpty() || nameEditText.text.toString() == user.name) {
                    nameEditText.setText(user.name)
                }
                
                // Only load the image if the user hasn't selected a new one
                if (selectedImageBitmap == null) {
                    if (user.profileImageUrl.isNotEmpty()) {
                        Picasso.get()
                            .load(user.profileImageUrl)
                            .fit()
                            .centerCrop()
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_person)
                    }
                }
                checkIfChanged()
            }
        }

        nameEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkIfChanged()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        userViewModel.fetchUser()

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_name_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.updateProfile(name, selectedImageBitmap) {
                Toast.makeText(requireContext(), getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                selectedImageBitmap = null // Clear selection to allow reloading from URL
            }
        }

        userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            
            if (isLoading) {
                saveButton.isEnabled = false
                saveButton.text = "Updating..."
            } else {
                saveButton.text = "Save"
                checkIfChanged()
            }
        }

        userViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                userViewModel.clearError()
            }
        }
    }
}
