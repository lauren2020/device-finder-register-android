package com.example.devicefinder.ui.main

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModelFactory(var context: Context?, var activity: Activity?) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass == MainViewModel::class.java) {
            return MainViewModel(context = context, activity = activity) as (T)
        }
        return super.create(modelClass)
    }
}
