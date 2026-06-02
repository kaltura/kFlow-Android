package com.kaltura.kflow.presentation.socialFacebookLogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentSocialFacebookBinding
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

class SocialFacebookLoginFragment : SharedTransitionFragment(R.layout.fragment_social_facebook) {
    override val feature = Feature.SOCIAL_FACEBOOK_LOGIN
    private val viewModel: SocialFacebookLoginViewModel by viewModel()
    private var _binding: FragmentSocialFacebookBinding? = null
    private val binding get() = _binding!!
    private var callbackManager: CallbackManager? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSocialFacebookBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Forward the login results back to the CallbackManager
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
        binding.facebook.setOnClickListener {
            withInternetConnection {
                clearDebugView()
                clearInputLayouts()
                if (binding.facebookAppId.text!!.isEmpty()) {
                    binding.facebookAppIdInputLayout.showError("Empty Facebook App ID")
                    return@withInternetConnection
                }
                if (binding.facebookSecret.text!!.isEmpty()) {
                    binding.facebookSecretInputLayout.showError("Empty Secret")
                    return@withInternetConnection
                }
                binding.facebook.startAnimation {
//                    binding.facebookAppId.setText("3507476506082657")
//                    binding.facebookSecret.setText("80798f2e9b62bcce2f4b701ef515d3da")
                viewModel.initiateFacebookSignInProcess(binding.facebookAppId.text.toString(),binding.facebookSecret.text.toString(),requireContext(),callbackManager)
                }
            }
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.facebookSignInEvent,
            error = {
                if (it.message.equals("Login Required")){
                    LoginManager.getInstance().logInWithReadPermissions(
                    this@SocialFacebookLoginFragment, // Activity or Fragment
                    listOf("email", "public_profile"))
                }else {
                    it.printStackTrace()
                    Toast.makeText(context, "Error : " + it.message, Toast.LENGTH_LONG).show()
                    binding.facebook.error(lifecycleScope)
                    binding.facebookToken.visibility = View.INVISIBLE
                }
            },
            success = {
                binding.facebook.success(lifecycleScope)
                binding.facebookToken.visibility = View.VISIBLE
                binding.facebookToken.text = it.toString()
            }
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun clearInputLayouts() {
        binding.facebookToken.text = ""
        binding.facebookAppIdInputLayout.hideError()
        binding.facebookSecretInputLayout.hideError()
    }

}