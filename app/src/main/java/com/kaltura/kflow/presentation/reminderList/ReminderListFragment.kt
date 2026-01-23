package com.kaltura.kflow.presentation.reminderList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.Reminder
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentRegistrationBinding
import com.kaltura.kflow.databinding.FragmentReminderListBinding
import com.kaltura.kflow.databinding.FragmentSnsBinding
import com.kaltura.kflow.presentation.base.BaseFragment
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.longToast
import com.kaltura.kflow.presentation.extension.observeResource
import com.kaltura.kflow.presentation.extension.toast
import com.kaltura.kflow.presentation.extension.withInternetConnection
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.ArrayList

class ReminderListFragment : SharedTransitionFragment(R.layout.fragment_reminder_list) {

    override val feature = Feature.REMINDERS
    private var reminderList = ArrayList<Reminder>()
    private val viewModel: ReminderListViewModel by viewModel()
    private val adapter = ReminderListAdapter().apply {
        deleteReminderClickListener = { reminder ->
            deleteReminderRequest(reminder)
        }
    }
    private var _binding: FragmentReminderListBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentReminderListBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRemindersRequest()
    }

    private fun initList() {
        binding.list.setHasFixedSize(true)
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.layoutAnimation =
            if (adapter.reminders.isEmpty()) {
                AnimationUtils.loadLayoutAnimation(context, R.anim.item_layout_animation)
            }
            else null
        binding.list.adapter = adapter
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
                reminderList = it
                initList()
                if (it.isEmpty()) longToast("Reminders List Empty")
            })
        observeResource(viewModel.deleteReminderEvent,
            error = {
                it.printStackTrace()
                longToast("Error while calling Reminders List : $it")
            },
            success = {
                toast("The following Reminder was deleted : $it")
                getRemindersRequest()
            })
    }
}