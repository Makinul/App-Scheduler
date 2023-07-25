package com.makinu.app.scheduler.ui.main.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.makinu.app.scheduler.R
import com.makinu.app.scheduler.data.model.AppInfo
import com.makinu.app.scheduler.data.model.AppUiInfo
import com.makinu.app.scheduler.databinding.ItemAppInfoBinding
import com.makinu.app.scheduler.utils.AppConstants.timeConversion

class AppInfoAdapter(
    private val list: List<AppUiInfo>,
    private val listener: OnClickListener
) :
    RecyclerView.Adapter<AppInfoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppInfoAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAppInfoBinding.inflate(
            inflater,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppInfoAdapter.ViewHolder, position: Int) {
        holder.bindData(holder.itemView.context, position)
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(
        private val binding: ItemAppInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                listener.clickOnView(layoutPosition, list[layoutPosition])
            }
        }

        fun bindData(context: Context, position: Int) {
            val item = list[position]

            binding.appName.text = item.appName
            binding.packageName.text = item.packageName
            binding.icon.setImageBitmap(item.icon)

            val color = if (item.isScheduled) {
                val scheduleTime = timeConversion(item.scheduleTime)
                binding.scheduleStatus.text =
                    context.getString(R.string.schedule_active, scheduleTime)
                ContextCompat.getColor(context, R.color.positive)
            } else {
                binding.scheduleStatus.text = context.getString(R.string.schedule_off)
                ContextCompat.getColor(context, R.color.negative)
            }
            binding.scheduleStatus.setTextColor(color)
        }
    }

    interface OnClickListener {
        fun clickOnView(position: Int, item: AppUiInfo)
    }
}


