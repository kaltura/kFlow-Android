package com.kaltura.kflow.presentation.ks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentKsBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class KsFragment : SharedTransitionFragment(R.layout.fragment_ks) {

    private val viewModel: KsViewModel by viewModel()

    override val feature = Feature.WORK_WITH_KS
    private var _binding: FragmentKsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentKsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.save.setOnClickListener {
            hideKeyboard()
            saveKs(binding.ks.string)
        }
    }

    override fun subscribeUI() {}

    private fun saveKs(ksValue: String) {
        if (ksValue.isEmpty()) {
            binding.ksInputLayout.showError("Empty KS")
            return
        }

        viewModel.saveKs(ksValue)

        toast("Saved")
    }
}