package com.kaltura.kflow.presentation.login

import android.os.Bundle
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.navArgs
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.ui.SharedTransition
import com.kaltura.kflow.utils.getUUID
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.find
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class LoginFragment : DebugFragment(R.layout.fragment_login) {

    private val args: LoginFragmentArgs by navArgs()
    private val viewModel: LoginViewModel by viewModel()

    override fun debugView(): DebugView = debugView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = SharedTransition()
        sharedElementReturnTransition = SharedTransition()
        exitTransition = Fade().setDuration(150L)
        enterTransition = Fade().setDuration(150L)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.find<View>(R.id.image)?.transitionName = args.transImage
        view?.find<View>(R.id.featureText)?.transitionName = args.transTitle
        view?.find<View>(R.id.header)?.transitionName = args.transBg
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header.background = args.background.toDrawable(resources)
        login.setOnClickListener {
            hideKeyboard()
            makeLoginRequest(username.string, password.string)
        }
        username.string = viewModel.getSavedUsername()
        password.string = viewModel.getSavedPassword()
    }

    override fun subscribeUI() {}

    private fun makeLoginRequest(email: String, password: String) {
        withInternetConnection {
            clearDebugView()
            viewModel.makeLoginRequest(email, password, getUUID(requireContext()))
        }
    }
}