package com.kaltura.kflow.presentation.socialLogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentSocialBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.extension.error
import com.kaltura.kflow.presentation.extension.hideError
import com.kaltura.kflow.presentation.extension.observeResource
import com.kaltura.kflow.presentation.extension.showError
import com.kaltura.kflow.presentation.extension.success
import com.kaltura.kflow.presentation.extension.withInternetConnection
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class SocialLoginFragment : SharedTransitionFragment(R.layout.fragment_social) {
    override val feature = Feature.SOCIAL_LOGIN
    private val viewModel: SocialLoginViewModel by viewModel()
    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSocialBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.google.setOnClickListener {
            withInternetConnection {
                clearDebugView()
                clearInputLayouts()
                if (binding.webClientId.text!!.isEmpty()) {
                    binding.webClientIdInputLayout.showError("Empty Web Client ID")
                    return@withInternetConnection
                }
                binding.google.startAnimation {
                    //viewModel.initiateGoogleSignInProcess(binding.webClientId.text.toString(),requireContext())
                    viewModel.initiateGoogleChooseAccountSignInProcess(binding.webClientId.text.toString(),requireContext())
                }
            }
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.googleSignInEvent,
            error = {
                it.printStackTrace()
                Toast.makeText(context, "Error : "+it.message, Toast.LENGTH_LONG).show()
                binding.google.error(lifecycleScope)
                binding.googleToken.visibility = View.INVISIBLE
            },
            success = {
                binding.google.success(lifecycleScope)
                binding.googleToken.visibility = View.VISIBLE
                binding.googleToken.text = it.toString()
            }
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun clearInputLayouts() {
        binding.googleToken.text = ""
        binding.webClientIdInputLayout.hideError()
    }
}