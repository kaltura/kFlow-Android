package com.kaltura.kflow.presentation.extension

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.kaltura.kflow.R
import org.jetbrains.anko.*

/**
 * Created by alex_litvinenko
 */

inline fun <reified T : Fragment> instanceOf(vararg params: Pair<String, Any>) = T::class.java.newInstance().apply {
    arguments = bundleOf(*params)
}

fun Fragment.replaceFragment(fragment: Fragment, containerId: Int = R.id.fragmentContainer, addToBackStack: Boolean = false) =
        requireActivity().replaceFragment(fragment, containerId, addToBackStack)

fun Fragment.getColor(@ColorRes id: Int) = ContextCompat.getColor(requireContext(), id)

fun Fragment.getDrawable(@DrawableRes id: Int) = ContextCompat.getDrawable(requireContext(), id)