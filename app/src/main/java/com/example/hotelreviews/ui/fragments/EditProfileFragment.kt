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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hotelreviews.R
import com.example.hotelreviews.viewmodel.UserViewModel
import com.squareup.picasso.Picasso

class EditProfileFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Picasso.get().load(it).into(profileImageView)
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
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImageView = view.findViewById(R.id.profile_image_view)
        nameEditText = view.findViewById(R.id.name_edit_text)
        saveButton = view.findViewById(R.id.save_button)
        progressBar = view.findViewById(R.id.progress_bar)
        val selectImageButton = view.findViewById<View>(R.id.select_image_button)

        selectImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        userViewModel.getCurrentUser()?.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                nameEditText.setText(user.name)
                if (user.profileImageUrl.isNotEmpty()) {
                    Picasso.get().load(user.profileImageUrl).placeholder(android.R.drawable.ic_menu_gallery).into(profileImageView)
                }
            }
        }

        userViewModel.refreshCurrentUser()

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_name_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.updateProfile(name, selectedImageBitmap) {
                Toast.makeText(requireContext(), getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            saveButton.isEnabled = !isLoading
        }

        userViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                userViewModel.clearError()
            }
        }
    }
}
