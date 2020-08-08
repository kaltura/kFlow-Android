package com.kaltura.kflow.presentation.extension

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.kaltura.client.types.APIException
import com.kaltura.kflow.utils.Resource
import com.kaltura.kflow.utils.hasInternetConnection

/**
 * Created by alex_litvinenko
 */

fun Fragment.navigateWithExtras(direction: NavDirections, vararg views: View = emptyArray()) {
    val sharedElements = views.map { it to it.transitionName }.toTypedArray()
    val extras = FragmentNavigatorExtras(*sharedElements)
    findNavController().navigate(direction, extras)
}


fun Fragment.navigate(direction: NavDirections, vararg additionalParams: Pair<String, Any>) {
    if (additionalParams.isNotEmpty()) {
        val bundle = direction.arguments
        bundle.putAll(bundleOf(*additionalParams))
        findNavController().navigate(direction.actionId, bundle)
    } else {
        findNavController().navigate(direction)
    }
}

inline fun <T> Fragment.observeResource(liveData: LiveData<Resource<T>>,
                                        crossinline error: (APIException) -> Unit = {},
                                        crossinline success: (T) -> Unit = {}) {
    liveData.observe(viewLifecycleOwner, Observer {
        when (it) {
            is Resource.Error -> error(it.ex)
            is Resource.Success -> success(it.data)
        }
    })
}

inline fun <T> Fragment.observeLiveData(liveData: LiveData<T>, crossinline block: (T) -> Unit = {}) {
    liveData.observe(viewLifecycleOwner, Observer { block(it) })
}

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

fun Fragment.isTv() = requireContext().isTv()
fun Fragment.runOnTv(action: () -> Unit) {
    if (isTv()) action()
}