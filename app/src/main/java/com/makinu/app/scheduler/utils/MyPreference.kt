package com.makinu.app.scheduler.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPreference @Inject constructor(
    @ApplicationContext context: Context
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

//    var isLoggedIn: Boolean
//        get() {
//            AppConstants.isLoggedUser = AppConstants.userId.isNotEmpty()
//            return AppConstants.isLoggedUser
//        }
//        set(value) {
//
//            val editor = sharedPreferences.edit()
//
//            editor.putString(KEY_PLAN_ID, value.id)
//
//            editor.apply()
//        }

    companion object {
        private const val TAG = "AppPreferences"
        private const val PREFERENCES_NAME = "appPreferences"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }
}