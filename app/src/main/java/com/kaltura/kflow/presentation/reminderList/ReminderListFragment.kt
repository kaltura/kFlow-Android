package com.kaltura.kflow.presentation.reminderList

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.Reminder
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.BaseFragment
import com.kaltura.kflow.presentation.extension.longToast
import com.kaltura.kflow.presentation.extension.observeResource
import com.kaltura.kflow.presentation.extension.withInternetConnection
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_reminder_list.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.ArrayList

class ReminderListFragment : BaseFragment(R.layout.fragment_reminder_list) {

    val feature = Feature.REMINDERS

    private var reminderList = ArrayList<Reminder>()
    private val viewModel: ReminderListViewModel by viewModel()
    private val adapter = ReminderListAdapter().apply {
        deleteReminderClickListener = { reminder ->
            deleteReminderRequest(reminder)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        getRemindersRequest()
    }

    private fun initList() {
        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.layoutAnimation =
            if (adapter.reminders.isEmpty()) AnimationUtils.loadLayoutAnimation(context, R.anim.item_layout_animation)
            else null
        list.adapter = adapter
        adapter.reminders = reminderList.toTypedArray()
    }

    private fun deleteReminderRequest(reminder: Reminder) {
        withInternetConnection {
            viewModel.makeDeleteReminderRequest(reminder)
        }
    }

    private fun getRemindersRequest() {
        withInternetConnection {
            viewModel.makeGetRemindersRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.reminderListEvent,
            error = {
                it.printStackTrace()
                longToast("Error while calling Reminders List : $it")
            },
            success = {
                if (it.isNotEmpty()) {
                    reminderList = it
                    initList()
                }else{
                    longToast("Reminders List Empty")
                }
            })
        observeResource(viewModel.deleteReminderEvent,
            error = {
                it.printStackTrace()
                longToast("Error while calling Reminders List : $it")
            },
            success = {
                longToast("The following Reminder was deleted : $it")
                getRemindersRequest()
            })
    }
}