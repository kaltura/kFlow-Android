package com.kaltura.kflow.presentation.appToken

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentAppTokenBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.utils.getUUID
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class AppTokenFragment : SharedTransitionFragment(R.layout.fragment_app_token) {

    private val viewModel: AppTokenViewModel by viewModel()

    override val feature = Feature.LOGIN_APP_TOKEN
    private var _binding: FragmentAppTokenBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentAppTokenBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.explicitLogin.setOnClickListener {
            hideKeyboard()
            makeExplicitLoginRequest(binding.username.string, binding.password.string)
        }
        binding.appToken.setOnClickListener {
            hideKeyboard()
            makeAppTokenStartSessionRequest()
        }
        binding.revokeSession.setOnClickListener {
            hideKeyboard()
            makeRevokeSessionRequest()
        }

        binding.username.string = viewModel.getSavedUsername()
        binding.password.string = viewModel.getSavedPassword()
    }

    override fun subscribeUI() {
        observeResource(viewModel.loginRequest,
                error = {
                    binding.explicitLogin.error(lifecycleScope)
                    binding.appToken.error(lifecycleScope)
                },
                success = {
                    binding.explicitLogin.success(lifecycleScope)
                    binding.appToken.success(lifecycleScope)
                    viewModel.saveUserCreds(binding.username.string, binding.password.string)
                }
        )
        observeResource(viewModel.revokeSessionRequest,
                error = { binding.revokeSession.error(lifecycleScope) },
                success = { binding.revokeSession.success(lifecycleScope) }
        )
        observeResource(viewModel.anonymousLoginRequest,
                error = {
                    binding.explicitLogin.error(lifecycleScope)
                    binding. appToken.error(lifecycleScope)
                },
                success = {
                    binding.explicitLogin.error(lifecycleScope)
                    binding.appToken.error(lifecycleScope)
                    Snackbar.make(requireView(), "Anonymous login!", Snackbar.LENGTH_LONG).show()
                }
        )
    }

    private fun makeExplicitLoginRequest(email: String, password: String) {
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

            binding.explicitLogin.startAnimation {
                viewModel.makeExplicitLoginRequest(email, password, getUUID())
            }
        }
    }

    private fun makeAppTokenStartSessionRequest() {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            binding.appToken.startAnimation {
                viewModel.makeAppTokenStartRequest(getUUID())
            }
        }
    }

    private fun makeRevokeSessionRequest() {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            binding.revokeSession.startAnimation {
                viewModel.makeRevokeSessionRequest()
            }
        }
    }

    private fun clearInputLayouts() {
        binding.usernameInputLayout.hideError()
        binding.passwordInputLayout.hideError()
    }
}