package com.makinu.app.scheduler.base

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.makinu.app.scheduler.R
import com.makinu.app.scheduler.utils.MyPreference

open class BaseFragment : Fragment() {
    protected lateinit var preference: MyPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preference = MyPreference(requireContext())
    }

    fun showSimpleDialog(@StringRes resourceId: Int = R.string.work_in_progress) {
        showSimpleDialog(getString(resourceId))
    }

    fun showSimpleDialog(message: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(
                R.string.ok
            ) { _: DialogInterface?, _: Int ->

            }
            .create()
        alertDialog.show()
    }

    fun showLog(@StringRes resourceId: Int) {
        Log.v(BaseActivity.TAG, getString(resourceId))
    }

    fun showLog(message: String = getString(R.string.test_log)) {
        Log.v(BaseActivity.TAG, message)
    }

    fun showToast(@StringRes resourceId: Int) {
        Toast.makeText(context, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    fun showToast(message: String = getString(R.string.work_in_progress)) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showProgressDialog(@StringRes messageResId: Int) {
        showProgressDialog("", getString(messageResId))
    }

    @JvmOverloads
    fun showProgressDialog(message: String? = getString(R.string.loading)) {
        showProgressDialog("", message)
    }

    @JvmOverloads
    fun showProgressDialog(title: String?, message: String?, cancelable: Boolean = false) {
        if (activity is BaseActivity) {
            (activity as BaseActivity?)!!.showProgressDialog(title, message, cancelable)
        } else {
            if (activity != null)
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
        }
    }

    fun closeProgressDialog() {
        if (activity is BaseActivity) {
            (activity as BaseActivity?)!!.closeProgressDialog()
        }
    }

    fun getProgressDialog(): ProgressDialog? {
        return if (activity is BaseActivity) {
            (activity as BaseActivity?)!!.getProgressDialog()
        } else {
            null
        }
    }

    fun updateProgressMessage(message: String) {
        if (activity is BaseActivity) {
            val progressDialog = (activity as BaseActivity?)!!.getProgressDialog()
            if (progressDialog != null && progressDialog.isShowing) {
                progressDialog.setTitle(message)
            }
        }
    }

    companion object {
        private const val TAG = "BaseFragment"
    }
}