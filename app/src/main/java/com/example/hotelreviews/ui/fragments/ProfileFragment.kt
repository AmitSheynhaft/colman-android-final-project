package com.example.hotelreviews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hotelreviews.R
import com.example.hotelreviews.databinding.FragmentProfileBinding
import com.example.hotelreviews.viewmodel.ProfileViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.editUsername.setText(it.username)
                binding.editFullname.setText(it.fullName)
                binding.editEmail.setText(it.email)
                binding.displayUsername.text = "@${it.username}"
                binding.displayName.text = it.fullName

                if (it.profileImageUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(it.profileImageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(binding.profileImage)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        binding.btnSave.setOnClickListener {
            val username = binding.editUsername.text.toString()
            val fullName = binding.editFullname.text.toString()
            val imageUrl = viewModel.user.value?.profileImageUrl ?: ""
            viewModel.updateProfile(username, fullName, imageUrl) {}
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
