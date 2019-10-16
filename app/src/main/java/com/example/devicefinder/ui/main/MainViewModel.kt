package com.example.devicefinder.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    fun registerDevice(code: String) {
        Log.d("REGISTER_DEVICE", code)
    }
}
