package com.makinu.app.scheduler.ui.main.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.makinu.app.scheduler.R
import com.makinu.app.scheduler.data.model.Scheduler
import com.makinu.app.scheduler.databinding.ItemAppSchedulerBinding
import com.makinu.app.scheduler.utils.AppConstants

class ScheduleAdapter(
    private val list: ArrayList<Scheduler>, private val listener: OnClickListener
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    private val checkedMap: HashMap<Int, Scheduler> = HashMap()

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

    private var lastItemSelectedPosition = -1

    inner class ViewHolder(
        private val binding: ItemAppSchedulerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                if (lastItemSelectedPosition != layoutPosition && lastItemSelectedPosition >= 0) {
                    notifyItemChanged(lastItemSelectedPosition)
                }
                if (itemView.isSelected && lastItemSelectedPosition == layoutPosition) {
                    lastItemSelectedPosition = -1
                }
                lastItemSelectedPosition = layoutPosition
                itemView.isSelected = !itemView.isSelected

                binding.switchButton.isChecked = itemView.isSelected
            }

            binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
                val item = list[layoutPosition]
                item.isScheduled = !item.isScheduled

                binding.appSchedule.isSelected = item.isScheduled
                binding.switchButton.isChecked = item.isScheduled

                if (checkedMap[layoutPosition] == null) {
                    checkedMap[layoutPosition] = item

                    binding.ok.visibility = View.VISIBLE
                    binding.cancel.visibility = View.VISIBLE
                    binding.cancel.setImageResource(R.drawable.ic_cross)
                } else {
                    checkedMap.remove(layoutPosition)
                }
                listener.clickOnView(layoutPosition, isChecked, item)
            }

            binding.ok.setOnClickListener {
                val updatedItem = checkedMap[layoutPosition]
                if (updatedItem != null) {
                    listener.onSaveScheduler(
                        layoutPosition,
                        updatedItem.isScheduled,
                        list[layoutPosition]
                    )
                }
            }
            binding.cancel.setOnClickListener {
                if (checkedMap[layoutPosition] == null) { // delete
                    listener.onDeleteScheduler(layoutPosition, list[layoutPosition])
                } else { // cancel
                    notifyItemChanged(layoutPosition)
                }
            }
        }

        fun bindData(context: Context, position: Int) {
            val item = list[position]

            val scheduleTime = AppConstants.timeConversion(item.scheduleTime)
            binding.appSchedule.text = context.getString(R.string.schedule_active, scheduleTime)

            val updatedItem = checkedMap[position]
            if (updatedItem != null) {
                binding.ok.visibility = View.VISIBLE
                binding.cancel.visibility = View.VISIBLE
                binding.cancel.setImageResource(R.drawable.ic_cross)
            } else {
                if (item.isScheduled) {
                    binding.ok.visibility = View.GONE
                    binding.cancel.visibility = View.VISIBLE
                    binding.cancel.setImageResource(R.drawable.ic_delete_scheduler)
                } else {
                    binding.cancel.visibility = View.GONE
                    binding.ok.visibility = View.GONE
                }
            }
            binding.appSchedule.isSelected = item.isScheduled
            binding.switchButton.isChecked = item.isScheduled

            itemView.isSelected = lastItemSelectedPosition == position
        }
    }

    interface OnClickListener {
        fun clickOnView(position: Int, isSelected: Boolean, item: Scheduler)
        fun onSaveScheduler(position: Int, isSelected: Boolean, item: Scheduler)
        fun onDeleteScheduler(position: Int, item: Scheduler)
    }
}


