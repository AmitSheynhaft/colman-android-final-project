package com.example.hotelreviews.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import com.example.hotelreviews.viewmodel.AuthViewModel
import com.squareup.picasso.Picasso

class RegisterFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val profileImageView = view?.findViewById<ImageView>(R.id.profile_image_view)
                Picasso.get().load(it).into(profileImageView)
                
                // Convert Uri to Bitmap
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
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameEditText = view.findViewById<EditText>(R.id.name_edit_text)
        val emailEditText = view.findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<EditText>(R.id.password_edit_text)
        val selectImageButton = view.findViewById<View>(R.id.select_image_button)
        val registerButton = view.findViewById<Button>(R.id.register_button)
        val loginText = view.findViewById<View>(R.id.login_text)
        val progressBar = view.findViewById<ProgressBar>(R.id.register_progress_bar)

        selectImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        loginText.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.register(email, password, name, selectedImageBitmap) {
                findNavController().navigate(R.id.action_registerFragment_to_myReviewsFragment)
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            registerButton.isEnabled = !isLoading
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                authViewModel.clearError()
            }
        }
    }
}
