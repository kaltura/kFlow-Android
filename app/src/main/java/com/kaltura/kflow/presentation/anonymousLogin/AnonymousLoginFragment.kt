package com.kaltura.kflow.presentation.anonymousLogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentAnonymousLoginBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.utils.getUUID
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class AnonymousLoginFragment : SharedTransitionFragment(R.layout.fragment_anonymous_login) {

    private val viewModel: AnonymousLoginViewModel by viewModel()

    override val feature = Feature.ANONYMOUS_LOGIN
    private var _binding: FragmentAnonymousLoginBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnonymousLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.login.setOnClickListener {
            hideKeyboard()
            makeAnonymousLoginRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.loginRequest,
                error = { binding.login.error(lifecycleScope) },
                success = { binding.login.success(lifecycleScope) }
        )
    }

    private fun makeAnonymousLoginRequest() {
        withInternetConnection {
            clearDebugView()
            binding.login.startAnimation {
                viewModel.anonymousLogin(getUUID())
            }
        }
    }
}