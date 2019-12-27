package com.kaltura.kflow.presentation.debug

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.extension.shareFile
import com.kaltura.kflow.utils.saveToFile
import org.json.JSONObject

/**
 * Created by alex_lytvynenko on 20.11.2018.
 */
abstract class DebugFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId), DebugListener {

    private var shareMenuItem: MenuItem? = null

    protected abstract fun debugView(): DebugView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_menu_items, menu)
        shareMenuItem = menu.findItem(R.id.fragment_menu_share)
        shareMenuItem?.isEnabled = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.fragment_menu_share) {
            share()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PhoenixApiManager.setDebugListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PhoenixApiManager.removeDebugListener()
    }

    override fun setRequestInfo(url: String, method: String, code: Int) {
        debugView().requestUrl = url
        debugView().requestMethod = method
        debugView().responseCode = code
    }

    override fun setRequestBody(jsonObject: JSONObject) {
        shareMenuItem?.isEnabled = true
        debugView().setRequestBody(jsonObject)
    }

    override fun setResponseBody(jsonObject: JSONObject) {
        debugView().setResponseBody(jsonObject)
    }

    override fun onError() {
        debugView().onUnknownError()
        shareMenuItem?.isEnabled = false
    }

    private fun share() {
        val file = saveToFile(requireContext(), debugView().sharedData)
        requireActivity().shareFile(file)
    }

    protected fun clearDebugView() {
        debugView().clear()
        shareMenuItem?.isEnabled = false
    }
}