package com.kaltura.kflow.presentation.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentCollectionsBinding
import com.kaltura.kflow.databinding.FragmentRecordingsBinding
import com.kaltura.kflow.databinding.FragmentRegistrationBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class RegistrationFragment : SharedTransitionFragment(R.layout.fragment_registration) {

    private val viewModel: RegistrationViewModel by viewModel()
    override val feature = Feature.REGISTRATION

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentRegistrationBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.register.setOnClickListener {
            hideKeyboard()
            makeRegistrationRequest(binding.firstName.string, binding.lastName.string, binding.username.string, binding.email.string, binding.password.string)
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.registerRequest,
                error = { binding.register.error(lifecycleScope) },
                success = { binding.register.success(lifecycleScope) }
        )
    }

    private fun makeRegistrationRequest(firstName: String, lastName: String, userName: String, email: String, password: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()
            if (firstName.isEmpty()) {
                binding.firstNameInputLayout.showError("Empty first name")
                return@withInternetConnection
            }
            if (lastName.isEmpty()) {
                binding.lastNameInputLayout.showError("Empty last name")
                return@withInternetConnection
            }
            if (userName.isEmpty()) {
                binding.userNameInputLayout.showError("Empty username")
                return@withInternetConnection
            }
            if (email.isEmpty()) {
                binding.emailInputLayout.showError("Empty email")
                return@withInternetConnection
            }
            if (password.isEmpty()) {
                binding.passwordInputLayout.showError("Empty password")
                return@withInternetConnection
            }
            binding.register.startAnimation {
                viewModel.register(firstName, lastName, userName, email, password)
            }
        }
    }

    private fun clearInputLayouts() {
        binding.firstNameInputLayout.hideError()
        binding.lastNameInputLayout.hideError()
        binding.userNameInputLayout.hideError()
        binding.emailInputLayout.hideError()
        binding.passwordInputLayout.hideError()
    }
}