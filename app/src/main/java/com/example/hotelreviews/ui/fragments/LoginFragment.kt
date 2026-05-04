package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hotelreviews.R
import com.example.hotelreviews.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText = view.findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<EditText>(R.id.password_edit_text)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val registerText = view.findViewById<TextView>(R.id.register_text)
        val progressBar = view.findViewById<ProgressBar>(R.id.login_progress_bar)
        val rememberMeCheckbox = view.findViewById<android.widget.CheckBox>(R.id.remember_me_checkbox)

        // Clear fields initially for safety after logout
        passwordEditText.setText("")

        // Load remembered email
        val prefs = requireContext().getSharedPreferences("login_prefs", android.content.Context.MODE_PRIVATE)
        val rememberedEmail = prefs.getString("remembered_email", "")
        if (!rememberedEmail.isNullOrEmpty()) {
            emailEditText.setText(rememberedEmail)
            rememberMeCheckbox.isChecked = true
        } else {
            emailEditText.setText("")
            rememberMeCheckbox.isChecked = false
        }

        // Only auto-navigate if we have a user AND we didn't just come here from a logout action
        // (Though signOut should be enough to make user null)
        authViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                findNavController().navigate(R.id.action_loginFragment_to_myReviewsFragment)
            }
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Handle "Remember Me" preference before logging in
            if (rememberMeCheckbox.isChecked) {
                prefs.edit().putString("remembered_email", email).apply()
            } else {
                prefs.edit().remove("remembered_email").apply()
            }

            authViewModel.login(email, password) {
                // Navigation is handled by the user observer
            }
        }

        registerText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            loginButton.isEnabled = !isLoading
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                authViewModel.clearError()
            }
        }
    }
}
