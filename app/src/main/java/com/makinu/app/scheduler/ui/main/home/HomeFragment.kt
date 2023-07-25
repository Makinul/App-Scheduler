package com.makinu.app.scheduler.ui.main.home

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.makinu.app.scheduler.R
import com.makinu.app.scheduler.base.BaseFragment
import com.makinu.app.scheduler.data.Status
import com.makinu.app.scheduler.data.model.AppUiInfo
import com.makinu.app.scheduler.databinding.DialogAppScheduleDetailsBinding
import com.makinu.app.scheduler.databinding.FragmentHomeBinding
import com.makinu.app.scheduler.utils.AppConstants
import com.makinu.app.scheduler.utils.ScheduleReceiver
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog

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
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.please_allow_permission)
            .setMessage(
                getString(
                    R.string.you_need_allow_overlay_permission_to_open_apps,
                    getString(R.string.app_name)
                )
            )
            .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                myIntent.data = Uri.parse("package:" + context?.packageName)
                startActivity(myIntent)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
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
//        viewModel.getAppStatus()
        context?.let { viewModel.getLauncherApps(it) }
    }

    private fun updateView() {
        if (items.isEmpty()) {
            showSimpleDialog(R.string.no_data_found)
        }
        adapter.notifyDataSetChanged()
    }

    private fun cancelAlarm(item: AppUiInfo) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, item.uid, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        if (alarmIntent != null && alarmManager != null) {
            alarmManager.cancel(alarmIntent)
        }
    }

    private fun setAlarm(item: AppUiInfo, hour: Int, minute: Int) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
            intent.putExtra(AppConstants.KEY_APP_UID, item.uid)
            intent.putExtra(AppConstants.KEY_APP_NAME, item.appName)
            intent.putExtra(AppConstants.KEY_PACKAGE_NAME, item.packageName)

            intent.putExtra(AppConstants.KEY_ALARM_HOUR, hour)
            intent.putExtra(AppConstants.KEY_ALARM_MINUTE, minute)

            PendingIntent.getBroadcast(context, item.uid, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        if (alarmIntent != null && alarmManager != null) {
            alarmManager.cancel(alarmIntent)
        }

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()

            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // it trigger approximate time
//        alarmManager?.setInexactRepeating(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            alarmIntent
//        )
//
//        val elapsedTime = SystemClock.elapsedRealtime()
//        val calendarTime = calendar.timeInMillis
//
//        alarmManager?.set(
//            AlarmManager.RTC_WAKEUP,
//            calendarTime,
//            alarmIntent
//        )

        // to schedule in exact time
        alarmManager?.setExact(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent
        )
    }

    private lateinit var loginDialog: Dialog

    fun showAppScheduleDialog(position: Int, appInfo: AppUiInfo) {
        if (!::loginDialog.isInitialized) {
            loginDialog = Dialog(context!!)
            loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

        val dBinding = DialogAppScheduleDetailsBinding.inflate(
            LayoutInflater.from(
                activity
            )
        )
        loginDialog.setContentView(dBinding.root)
        val window = loginDialog.window
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        loginDialog.setCanceledOnTouchOutside(true)
        if (!loginDialog.isShowing) loginDialog.show()

        dBinding.appName.text = appInfo.appName
        dBinding.packageName.text = appInfo.packageName
        dBinding.icon.setImageBitmap(appInfo.icon)

        if (appInfo.isScheduled) {
            dBinding.timePicker.visibility = View.VISIBLE

            appInfo.scheduleTime?.let {
                if (it.contains(":")) {
                    val parts = it.split(":")
                    if (parts.size > 1) {
                        try {
                            val hour = parts[0].toInt()
                            val minute = parts[1].toInt()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                dBinding.timePicker.hour = hour
                                dBinding.timePicker.minute = minute
                            } else {
                                dBinding.timePicker.currentHour = hour
                                dBinding.timePicker.currentMinute = minute
                            }
                        } catch (e: java.lang.NumberFormatException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else {
            dBinding.timePicker.visibility = View.GONE
        }
        dBinding.switchButton.isChecked = appInfo.isScheduled

        dBinding.switchButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dBinding.timePicker.visibility = View.VISIBLE
            } else {
                dBinding.timePicker.visibility = View.GONE
            }
        }

        dBinding.okButton.setOnClickListener {
            val hour: Int
            val minute: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = dBinding.timePicker.hour
                minute = dBinding.timePicker.minute
            } else {
                hour = dBinding.timePicker.currentHour
                minute = dBinding.timePicker.currentMinute
            }
            appInfo.scheduleTime = if (hour > 9) {
                "$hour"
            } else {
                "0$hour"
            }
            appInfo.scheduleTime += if (minute > 9) {
                ":$minute"
            } else {
                ":0$minute"
            }

            // Todo, need few more time finish the following task
            // we need to check here time conflicting issue
            // while saving the data into database and set alarm

            appInfo.isScheduled = dBinding.switchButton.isChecked
            val message = if (appInfo.isScheduled) {
                viewModel.setAlarm(appInfo)
                setAlarm(appInfo, hour, minute)
                getString(R.string.schedule_set_for, appInfo.appName)
            } else {
                viewModel.cancelAlarm(appInfo.packageName)
                cancelAlarm(appInfo)
                getString(R.string.schedule_cancel_for, appInfo.appName)
            }
            showToast(message)
            loginDialog.dismiss()

            items[position] = appInfo
            adapter.notifyItemChanged(position, true)
        }

        dBinding.cancelButton.setOnClickListener {
            loginDialog.dismiss()
        }

        dBinding.cross.setOnClickListener {
            loginDialog.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}