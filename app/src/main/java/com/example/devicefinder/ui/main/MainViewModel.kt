package com.example.devicefinder.ui.main

import android.content.Context
import android.telephony.TelephonyManager

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import android.Manifest.permission.READ_PHONE_STATE
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.HashMap


class MainViewModel(
    var context: Context?,
    var activity: Activity?
) : ViewModel() {
    val showAlertMessage: MutableLiveData<String> = MutableLiveData()
    val requestPermission: MutableLiveData<String> = MutableLiveData()

    fun handleShowMessage(message: String) {
        activity?.runOnUiThread(Runnable() {
            showAlertMessage.value = message
        })
    }


    fun registerDevice(code: String) {
        Log.d("REGISTER_DEVICE", code)
        val imei = getIMEINumber()

        if (imei == null) {
            showAlertMessage.value = "Error: Device IMEI number was unable to be retrieved. Check your permissions and try again."
            return
        }
        val params = getParams(imei, code)
        doAsync() {
            sendRegistrationPost(params)
        }.execute()
    }

    private fun getIMEINumber(): String? {
        //Testing only
        return "testImei12345"

        val unwrappedContext = context ?: return null
        try{
            val permission = ContextCompat.checkSelfPermission(unwrappedContext, READ_PHONE_STATE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                requestPermission.value = READ_PHONE_STATE
            }

            val tm = unwrappedContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val IMEI = tm.imei
            Log.d("REGISTER_DEVICE", "Found IMEI: " + IMEI)
            return IMEI

        }catch (ex: Exception){
            Log.e("REGISTER_DEVICE", "Error retrieving IMEI: " + ex.message)
        }
        return null
    }

    private fun getParams(imei: String, code: String): Map<String, String> {
        val deviceData: HashMap<String, String> = HashMap()
        if(code == "") {
            showAlertMessage.value = "Error: Please enter a code" // After some testing I can see that there is a toast that appears but the app crashes due to another bug
            // Need to do something here to prevent there from being empty codes
        }
        deviceData["imei"] = imei
        deviceData["code"] = code

        return  deviceData
    }

    private fun sendRegistrationPost(params: Map<String, String>) {
        var outputWriter: OutputStreamWriter? = null
        val url = "https://www.device-finder.com/api/v1/register"
        try {
            val urlret = URL(url)
                .openConnection()
                .let {
                    it as HttpURLConnection
                }.apply {
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    requestMethod = "POST"

                    doOutput = true
                    outputWriter = OutputStreamWriter(outputStream)
                    outputWriter?.write(JSONObject(params).toString())
                    outputWriter?.flush()
                }.let {
                    Log.d("REGISTER_DEVICE", "http url connection")
                    if (it.responseCode >= 200 && it.responseCode < 300) it.inputStream else it.errorStream
                }.let { streamToRead ->
                    BufferedReader(InputStreamReader(streamToRead)).use {
                        val response = StringBuffer()

                        //handleShowMessage("Your device has been successfully registered.")

                        var inputLine = it.readLine()
                        while (inputLine != null) {
                            response.append(inputLine)
                            inputLine = it.readLine()
                        }
                        it.close()
                        response.toString()
                    }
                }
            handleShowMessage(urlret)
        } catch (e: Exception) {
            handleShowMessage("There was an error registering your device! Please try again.")
            Log.d("REGISTER_DEVICE", "Error: " + e.toString())
        } finally {
            outputWriter?.close()
        }
    }




}


