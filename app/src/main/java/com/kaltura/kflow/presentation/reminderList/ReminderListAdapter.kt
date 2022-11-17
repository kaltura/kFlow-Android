package com.kaltura.kflow.presentation.reminderList
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.client.types.AssetReminder
import com.kaltura.client.types.Reminder
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_reminder.*

class ReminderListAdapter : RecyclerView.Adapter<ReminderListAdapter.MyViewHolder>() {

    var deleteReminderClickListener: (reminder: Reminder) -> Unit = {}

    var reminders: Array<Reminder> = arrayOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent.inflate(
        R.layout.item_reminder))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(reminders[position])

    override fun getItemCount() = reminders.size

    inner class MyViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(reminder: Reminder) {

            reminder_name.text = reminder.name
            reminder_id.text = reminder.id.toString()
            deleteReminder.setOnClickListener{ deleteReminderClickListener(reminder) }

        }
    }
}