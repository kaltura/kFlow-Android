package com.kaltura.kflow.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentMainBinding
import com.kaltura.kflow.presentation.extension.isTv
import com.kaltura.kflow.presentation.extension.navigateWithExtras

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class MainFragment : Fragment(R.layout.fragment_main) {

    private val features = arrayOf(Feature.LOGIN, Feature.LOGIN_APP_TOKEN, Feature.WORK_WITH_KS,
            Feature.ANONYMOUS_LOGIN, Feature.REGISTRATION, Feature.COLLECTIONS, Feature.VOD,
            Feature.CONTINUE_WATCHING, Feature.EPG, Feature.LIVE, Feature.FAVORITES, Feature.SEARCH,
            Feature.KEEP_ALIVE, Feature.MEDIA_PAGE, Feature.SUBSCRIPTION, Feature.PRODUCT_PRICE,
            Feature.CHECK_RECEIPT, Feature.TRANSACTION_HISTORY, Feature.RECORDINGS, Feature.BOOKMARK,
            Feature.IOT, Feature.DEVICE_MANAGEMENT,Feature.SNS,Feature.REMINDERS, Feature.SETTINGS)

    private lateinit var rotationAnimation: SpringAnimation
    private var isDragging = false
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (isDragging) {
                rotationAnimation.cancel()
                binding.kaltura.rotation += -(dy.toFloat() / 2)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            isDragging = newState == RecyclerView.SCROLL_STATE_DRAGGING
            if (newState == RecyclerView.SCROLL_STATE_SETTLING) rotationAnimation.start()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentMainBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        _binding = FragmentMainBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        initSpringAnimation()
        initList()
    }

    private fun initList() {
        binding.list.setHasFixedSize(true)
        binding.list.layoutManager = GridLayoutManager(requireContext(), if (isTv()) 4 else 2)
        val adapter = FeatureAdapter(features)
        adapter.clickListener = { feature, image, title ->
            navigateWithExtras(when (feature) {
                Feature.LOGIN -> MainFragmentDirections.navigateToLogin()
                Feature.LOGIN_APP_TOKEN -> MainFragmentDirections.navigateToAppToken()
                Feature.WORK_WITH_KS -> MainFragmentDirections.navigateToKs()
                Feature.ANONYMOUS_LOGIN -> MainFragmentDirections.navigateToAnonymousLogin()
                Feature.REGISTRATION -> MainFragmentDirections.navigateToRegistration()
                Feature.COLLECTIONS -> MainFragmentDirections.navigateToCollections()
                Feature.VOD -> MainFragmentDirections.navigateToVod()
                Feature.CONTINUE_WATCHING -> MainFragmentDirections.navigateToContinueWatching()
                Feature.EPG -> MainFragmentDirections.navigateToEpg()
                Feature.LIVE -> MainFragmentDirections.navigateToLiveTv()
                Feature.FAVORITES -> MainFragmentDirections.navigateToFavorites()
                Feature.SEARCH -> MainFragmentDirections.navigateToSearch()
                Feature.KEEP_ALIVE -> MainFragmentDirections.navigateToKeepAlive()
                Feature.MEDIA_PAGE -> MainFragmentDirections.navigateToMediaPage()
                Feature.SUBSCRIPTION -> MainFragmentDirections.navigateToSubscription()
                Feature.PRODUCT_PRICE -> MainFragmentDirections.navigateToProductPrice()
                Feature.CHECK_RECEIPT -> MainFragmentDirections.navigateToCheckReceipt()
                Feature.TRANSACTION_HISTORY -> MainFragmentDirections.navigateToTransactionHistory()
                Feature.RECORDINGS -> MainFragmentDirections.navigateToRecordings()
                Feature.BOOKMARK -> MainFragmentDirections.navigateToBookmark()
                Feature.IOT -> MainFragmentDirections.navigateToIot()
                Feature.DEVICE_MANAGEMENT -> MainFragmentDirections.navigateToDeviceManagement()
                Feature.REMINDERS -> MainFragmentDirections.navigateToReminders()
                Feature.SNS -> MainFragmentDirections.navigateToSns()
                Feature.SETTINGS -> MainFragmentDirections.navigateToSettings()
            }, image, title)
        }
        binding.list.adapter = adapter
        binding.list.addOnScrollListener(scrollListener)
    }

    private fun initSpringAnimation() {
        rotationAnimation = SpringAnimation(binding.kaltura, SpringAnimation.ROTATION).apply {
            spring = SpringForce(0f).apply {
                stiffness = SpringForce.STIFFNESS_LOW
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.list.removeOnScrollListener(scrollListener)
        _binding = null
    }
}