package com.makinu.app.scheduler.ui.main.home

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.makinu.app.scheduler.R
import com.makinu.app.scheduler.base.BaseFragment
import com.makinu.app.scheduler.data.Status
import com.makinu.app.scheduler.data.model.AppUiInfo
import com.makinu.app.scheduler.data.model.Scheduler
import com.makinu.app.scheduler.databinding.DialogAppScheduleDetailsBinding
import com.makinu.app.scheduler.databinding.FragmentHomeBinding
import com.makinu.app.scheduler.utils.AppConstants
import com.makinu.app.scheduler.utils.ScheduleReceiver
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        prepareRecyclerView()
        return binding.root
    }

    private val items: ArrayList<AppUiInfo> = ArrayList()
    private lateinit var adapter: AppInfoAdapter

    private fun prepareRecyclerView() {
        adapter = AppInfoAdapter(items, object : AppInfoAdapter.OnClickListener {
            override fun clickOnView(position: Int, item: AppUiInfo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(context)) {
                        showOverlayPermissionDialog()
                        return
                    }
                }
                showAppScheduleDialog(position, item)
            }
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity(), VERTICAL, false)
            adapter = this@HomeFragment.adapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showOverlayPermissionDialog() {
        val alertDialog =
            AlertDialog.Builder(requireContext()).setTitle(R.string.please_allow_permission)
                .setMessage(
                    getString(
                        R.string.you_need_allow_overlay_permission_to_open_apps,
                        getString(R.string.app_name)
                    )
                ).setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                    val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    myIntent.data = Uri.parse("package:" + context?.packageName)
                    startActivity(myIntent)
                }.setNegativeButton(R.string.cancel, null).create()
        alertDialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.appUiInfos.observe(viewLifecycleOwner) { response ->
            response.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgressDialog()
                    }
                    Status.ERROR -> {
                        closeProgressDialog()
                        it.message?.let { message -> showSimpleDialog(message) }
                            ?: showSimpleDialog(R.string.no_data_found)
                    }
                    Status.SUCCESS -> {
                        closeProgressDialog()
                        items.clear()
                        it.data?.let { list ->
                            items.addAll(list)
                        }
                        updateView()
                    }
                }
            }
        }

        context?.let { viewModel.getLauncherApps(it) }
    }

    private fun updateView() {
        if (items.isEmpty()) {
            showSimpleDialog(R.string.no_data_found)
        }
        adapter.notifyDataSetChanged()
    }

    private fun cancelAlarm(item: Scheduler) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, item.id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        if (alarmIntent != null && alarmManager != null) {
            alarmManager.cancel(alarmIntent)
        }
    }

    private fun setAlarm(item: Scheduler, calendar: Calendar) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
            intent.putExtra(AppConstants.KEY_SCHEDULER_ID, item.id)
            intent.putExtra(AppConstants.KEY_APP_UID, item.uid)
            intent.putExtra(AppConstants.KEY_APP_NAME, item.appName)
            intent.putExtra(AppConstants.KEY_PACKAGE_NAME, item.packageName)

            intent.putExtra(AppConstants.KEY_ALARM_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
            intent.putExtra(AppConstants.KEY_ALARM_MINUTE, calendar.get(Calendar.MINUTE))

            PendingIntent.getBroadcast(context, item.id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        if (alarmIntent != null && alarmManager != null) {
            alarmManager.cancel(alarmIntent)
        }

        // to schedule in exact time
        alarmManager?.setExact(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent
        )
    }

    private var schedulerAdapter: ScheduleAdapter? = null

    fun showAppScheduleDialog(position: Int, appInfo: AppUiInfo) {
        val schedulerDialog = Dialog(context!!)
        schedulerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dBinding = DialogAppScheduleDetailsBinding.inflate(
            LayoutInflater.from(
                activity
            )
        )
        schedulerDialog.setContentView(dBinding.root)
        val window = schedulerDialog.window
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        schedulerDialog.setCanceledOnTouchOutside(true)
        if (!schedulerDialog.isShowing) schedulerDialog.show()

        dBinding.appName.text = appInfo.appName
        dBinding.packageName.text = appInfo.packageName
        dBinding.icon.setImageBitmap(appInfo.icon)

        val schedulers: ArrayList<Scheduler> = ArrayList()
        schedulerAdapter = ScheduleAdapter(schedulers, object : ScheduleAdapter.OnClickListener {
            override fun clickOnView(position: Int, isSelected: Boolean, item: Scheduler) {
                val currentTime = AppConstants.timeToCalendar(item.scheduleTime)
                val currentHour = currentTime[Calendar.HOUR_OF_DAY]
                val currentMin = currentTime[Calendar.MINUTE]

                val timePickerDialog =
                    TimePickerDialog(
                        context, { view, hourOfDay, minute ->
                            val currentTimeAgain = Calendar.getInstance()
                            currentTimeAgain.set(Calendar.SECOND, 0)

                            val setTime = Calendar.getInstance()
                            setTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            setTime.set(Calendar.MINUTE, minute)
                            setTime.set(Calendar.SECOND, 0)

                            if (setTime.timeInMillis <= currentTimeAgain.timeInMillis) {
                                setTime.add(Calendar.DAY_OF_MONTH, 1)
                            }

                            val scheduler = Scheduler()
                            scheduler.isScheduled = false
                            scheduler.id = item.id
                            scheduler.scheduleTime = "$hourOfDay:$minute"
                            scheduler.scheduleRunning = true
                            scheduler.uid = item.uid
                            scheduler.appName = appInfo.appName
                            scheduler.packageName = appInfo.packageName

                            viewModel.schedulerId.observe(viewLifecycleOwner) { event ->
                                event.getContentIfNotHandled()?.let {
                                    val id: Int = (it.data ?: 0).toInt()
                                    if (it.status == Status.ERROR || id == 0) {
                                        val message =
                                            it.message ?: "Unknown error, please try again"
                                        showSimpleDialog(message)
                                        return@observe
                                    }

                                    scheduler.id = id
                                    schedulers[position] = scheduler

                                    setAlarm(
                                        scheduler,
                                        calendar = setTime
                                    )
                                    schedulerAdapter?.notifyItemChanged(position)
                                }
                            }
                            viewModel.updateScheduler(scheduler)
                        },
                        currentHour,
                        currentMin,
                        true
                    )
                timePickerDialog.setTitle(R.string.app_schedule_title)
                timePickerDialog.show()
            }

            override fun onSaveScheduler(position: Int, isSelected: Boolean, item: Scheduler) {
                item.scheduleRunning = isSelected
                schedulers[position] = item

                schedulerAdapter?.notifyItemChanged(position)
                viewModel.updateScheduler(item)

                val currentTimeAgain = Calendar.getInstance()
                currentTimeAgain.set(Calendar.SECOND, 0)

                val setTime = AppConstants.timeToCalendar(item.scheduleTime)

                if (setTime.timeInMillis <= currentTimeAgain.timeInMillis) {
                    setTime.add(Calendar.DAY_OF_MONTH, 1)
                }

                if (isSelected) {
                    setAlarm(
                        item,
                        calendar = setTime
                    )
                } else {
                    cancelAlarm(item)
                }
            }

            override fun onDeleteScheduler(position: Int, item: Scheduler) {
                schedulers.removeAt(position)
                schedulerAdapter?.notifyItemRemoved(position)
                viewModel.deleteScheduler(item)

                cancelAlarm(item)
            }
        })

        dBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity(), VERTICAL, false)
            adapter = schedulerAdapter
        }

        viewModel.schedulers.observe(viewLifecycleOwner) { event ->
            event.peekContent().let {
                if (it.status == Status.SUCCESS) {
                    schedulers.clear()
                    it.data?.let { list ->
                        schedulers.addAll(list)
                    }
                    schedulerAdapter?.notifyDataSetChanged()
                }
            }
        }
        viewModel.getSchedulers(appInfo)

        dBinding.add.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val currentHour = currentTime[Calendar.HOUR_OF_DAY]
            val currentMin = currentTime[Calendar.MINUTE]

            val timePickerDialog =
                TimePickerDialog(
                    context, { view, hourOfDay, minute ->
                        val currentTimeAgain = Calendar.getInstance()
                        currentTimeAgain.set(Calendar.SECOND, 0)

                        val setTime = Calendar.getInstance()
                        setTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        setTime.set(Calendar.MINUTE, minute)
                        setTime.set(Calendar.SECOND, 0)

                        if (setTime.timeInMillis <= currentTimeAgain.timeInMillis) {
                            setTime.add(Calendar.DAY_OF_MONTH, 1)
                        }

                        val scheduler = Scheduler()
                        scheduler.isScheduled = false
                        scheduler.scheduleTime = "$hourOfDay:$minute"
                        scheduler.scheduleRunning = true
                        scheduler.uid = appInfo.uid
                        scheduler.appName = appInfo.appName
                        scheduler.packageName = appInfo.packageName

                        viewModel.schedulerId.observe(viewLifecycleOwner) { event ->
                            event.getContentIfNotHandled()?.let {
                                val id: Int = (it.data ?: 0).toInt()
                                if (it.status == Status.ERROR || id == 0) {
                                    val message = it.message ?: "Unknown error, please try again"
                                    showSimpleDialog(message)
                                    return@observe
                                }

                                scheduler.id = id
                                schedulers.add(scheduler)

                                setAlarm(
                                    scheduler,
                                    calendar = setTime
                                )
                                schedulerAdapter?.notifyItemInserted(schedulers.size - 1)
                            }
                        }
                        viewModel.insertScheduler(scheduler)
                    },
                    currentHour,
                    currentMin,
                    true
                )
            timePickerDialog.setTitle(R.string.app_schedule_title)
            timePickerDialog.show()
        }

        dBinding.okButton.setOnClickListener {
            schedulerDialog.dismiss()
        }

        dBinding.cancelButton.setOnClickListener {
            schedulerDialog.dismiss()
        }

        dBinding.cross.setOnClickListener {
            schedulerDialog.dismiss()
        }

        schedulerDialog.setOnDismissListener {
            updateItem(position, appInfo.packageName)
        }
    }

    private fun updateItem(position: Int, packageName: String) {
        viewModel.scheduleCounter.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                if (it.status == Status.SUCCESS) {
                    it.data?.let { counter ->
                        if (it.extraValue > -1 && it.extraValue < items.size) {
                            val item = items[it.extraValue]
                            item.scheduleCounter = counter
                            items[it.extraValue] = item
                            adapter.notifyItemChanged(it.extraValue)
                        }
                    }
                }
            }
        }
        viewModel.getSchedulersByPackageName(position, packageName)
    }

    private fun prepareSchedulerToSave(hour: Int, minute: Int, scheduler: Scheduler) {
        scheduler.scheduleTime = if (hour > 9) {
            "$hour"
        } else {
            "0$hour"
        }
        scheduler.scheduleTime += if (minute > 9) {
            ":$minute"
        } else {
            ":0$minute"
        }

//        viewModel.setAlarm()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}