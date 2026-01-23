package com.kaltura.kflow.presentation.reminderList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.client.types.Reminder
import com.kaltura.kflow.databinding.ItemReminderBinding
import kotlinx.android.extensions.LayoutContainer


class ReminderListAdapter : RecyclerView.Adapter<ReminderListAdapter.MyViewHolder>() {

    var deleteReminderClickListener: (reminder: Reminder) -> Unit = {}

    var reminders: Array<Reminder> = arrayOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val bindMe = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(bindMe, parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(reminders[position])

    override fun getItemCount() = reminders.size

    private var _binding: ItemReminderBinding? = null
    private val binding get() = _binding!!
    inner class MyViewHolder(val binding: ItemReminderBinding, override val containerView: View) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

        fun bind(reminder: Reminder) {

            binding.reminderName.text = reminder.name
            binding.reminderId.text = reminder.id.toString()
            binding.deleteReminder.setOnClickListener { deleteReminderClickListener(reminder) }

        }
    }
}