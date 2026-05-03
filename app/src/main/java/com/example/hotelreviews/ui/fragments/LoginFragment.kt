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

        if (authViewModel.user.value != null) {
            findNavController().navigate(R.id.action_loginFragment_to_myReviewsFragment)
            return
        }

        val emailEditText = view.findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<EditText>(R.id.password_edit_text)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val registerText = view.findViewById<TextView>(R.id.register_text)
        val progressBar = view.findViewById<ProgressBar>(R.id.login_progress_bar)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.login(email, password) {
                findNavController().navigate(R.id.action_loginFragment_to_myReviewsFragment)
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
