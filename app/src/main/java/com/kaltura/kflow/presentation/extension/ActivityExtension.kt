package com.kaltura.kflow.presentation.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.kaltura.kflow.R

/**
 * Created by alex_litvinenko
 */
fun FragmentActivity.addFragment(fragment: Fragment, containerId: Int, addToBackStack: Boolean = false) {
    if (addToBackStack) supportFragmentManager.beginTransaction()
            .add(containerId, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    else supportFragmentManager.beginTransaction()
            .add(containerId, fragment)
            .commitAllowingStateLoss()
}

fun FragmentActivity.replaceFragment(fragment: Fragment, containerId: Int = R.id.fragmentContainer, addToBackStack: Boolean = false) =
        if (addToBackStack) supportFragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        else supportFragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commitAllowingStateLoss()

fun FragmentActivity.closeFragment() {
    supportFragmentManager.popBackStack()
}