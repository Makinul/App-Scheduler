package com.makinu.app.scheduler.ui.main.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.makinu.app.scheduler.R
import com.makinu.app.scheduler.data.model.Scheduler
import com.makinu.app.scheduler.databinding.ItemAppSchedulerBinding
import com.makinu.app.scheduler.utils.AppConstants

class ScheduleAdapter(
    private val list: List<Scheduler>, private val listener: OnClickListener
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAppSchedulerBinding.inflate(
            inflater, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleAdapter.ViewHolder, position: Int) {
        holder.bindData(holder.itemView.context, position)
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(
        private val binding: ItemAppSchedulerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                listener.clickOnView(
                    layoutPosition,
                    binding.switchButton.isChecked,
                    list[layoutPosition]
                )
            }

            binding.switchButton.setOnClickListener {
                listener.onSaveScheduler(
                    layoutPosition,
                    binding.switchButton.isChecked,
                    list[layoutPosition]
                )
            }

            binding.delete.setOnClickListener {
                listener.onDeleteScheduler(layoutPosition, list[layoutPosition])
            }
        }

        fun bindData(context: Context, position: Int) {
            val item = list[position]

            binding.appSchedule.isSelected = item.scheduleRunning
            binding.switchButton.isChecked = item.scheduleRunning

            val scheduleTime = AppConstants.timeConversion(item.scheduleTime)

            val status = if (item.scheduleRunning) {
                context.getString(R.string.schedule_active, "at $scheduleTime")
            } else {
                if (item.isScheduled)
                    context.getString(R.string.schedule_done, "at $scheduleTime")
                else
                    context.getString(R.string.schedule_canceled, scheduleTime)
            }
            binding.appSchedule.text = status
        }
    }

    interface OnClickListener {
        fun clickOnView(position: Int, isSelected: Boolean, item: Scheduler)
        fun onSaveScheduler(position: Int, isSelected: Boolean, item: Scheduler)
        fun onDeleteScheduler(position: Int, item: Scheduler)
    }
}


