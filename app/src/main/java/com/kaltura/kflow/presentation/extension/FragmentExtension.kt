package com.kaltura.kflow.presentation.extension

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.google.android.material.snackbar.Snackbar
import com.kaltura.kflow.R
import com.kaltura.kflow.utils.hasInternetConnection

/**
 * Created by alex_litvinenko
 */

inline fun <reified T : Fragment> instanceOf(vararg params: Pair<String, Any>): T = T::class.java.newInstance().apply {
    arguments = bundleOf(*params)
}

fun Fragment.replaceFragment(fragment: Fragment, containerId: Int = R.id.fragmentContainer, addToBackStack: Boolean = false) =
        requireActivity().replaceFragment(fragment, containerId, addToBackStack)

fun Fragment.getColor(@ColorRes id: Int) = ContextCompat.getColor(requireContext(), id)

fun Fragment.getDrawable(@DrawableRes id: Int) = ContextCompat.getDrawable(requireContext(), id)

fun Fragment.withInternetConnection(doBlock: () -> Unit) {
    if (hasInternetConnection(requireContext())) {
        doBlock()
    } else {
        Snackbar.make(view!!, "No Internet connection", Snackbar.LENGTH_LONG)
                .setAction("Dismiss") { }
                .show()
    }
}

fun Fragment.getQuantityString(@PluralsRes id: Int, count: Int): String = requireContext().getQuantityString(id, count)

fun Fragment.hideKeyboard() {
    view?.let {
        it.context.inputManager?.hideSoftInputFromWindow(it.rootView.windowToken, 0)
    }
}

fun Fragment.showKeyboard(view: View) {
    view.requestFocus()
    view.context.inputManager?.showSoftInput(view, 0)
}