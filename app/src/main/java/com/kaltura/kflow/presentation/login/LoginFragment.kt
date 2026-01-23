package com.kaltura.kflow.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.StringValue
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentLoginBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.utils.getUUID
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class LoginFragment : SharedTransitionFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModel()

    override val feature = Feature.LOGIN
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentLoginBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.login.setOnClickListener {
            hideKeyboard()
            makeLoginRequest(binding.username.string, binding.password.string, binding.extraParamsKey.string,
                binding.extraParamsDescription.string, binding.extraParamsValue.string)
        }
        binding.addExtraParams.setOnClickListener { showExtraParams(true) }
        binding.removeExtraParams.setOnClickListener { showExtraParams(false) }

        binding.username.string = viewModel.getSavedUsername()
        binding.password.string = viewModel.getSavedPassword()
    }

    override fun subscribeUI() {
        observeResource(viewModel.loginRequest,
                error = { binding.login.error(lifecycleScope) },
                success = { binding.login.success(lifecycleScope) }
        )
    }

    private fun makeLoginRequest(email: String, password: String, extraParamsKey: String,
                                 extraParamsDescription: String, extraParamsValue: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()
            if (email.isEmpty()) {
                binding.usernameInputLayout.showError("Empty username")
                return@withInternetConnection
            }
            if (password.isEmpty()) {
                binding.passwordInputLayout.showError("Empty password")
                return@withInternetConnection
            }

            var extraParams: HashMap<String, StringValue>? = null
            if (extraParamsKey.isNotEmpty() && extraParamsDescription.isNotEmpty() && extraParamsValue.isNotEmpty())
                extraParams = hashMapOf(extraParamsKey to StringValue().apply {
                    description = extraParamsDescription
                    value = extraParamsValue
                })

            binding.login.startAnimation {
                viewModel.makeLoginRequest(email, password, getUUID(), extraParams)
            }
        }
    }

    private fun showExtraParams(isShow: Boolean) {
        binding.addExtraParams.visibleOrGone(isShow.not())
        binding.removeExtraParams.visibleOrGone(isShow)
        binding.extraParamsKeyInputLayout.visibleOrGone(isShow)
        binding.extraParamsDescriptionInputLayout.visibleOrGone(isShow)
        binding.extraParamsValueInputLayout.visibleOrGone(isShow)

        if (isShow.not()) {
            binding.extraParamsKey.text?.clear()
            binding.extraParamsDescription.text?.clear()
            binding.extraParamsValue.text?.clear()
        }
    }

    private fun clearInputLayouts() {
        binding.usernameInputLayout.hideError()
        binding.passwordInputLayout.hideError()
        binding.extraParamsKeyInputLayout.hideError()
        binding.extraParamsDescriptionInputLayout.hideError()
        binding.extraParamsValueInputLayout.hideError()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}