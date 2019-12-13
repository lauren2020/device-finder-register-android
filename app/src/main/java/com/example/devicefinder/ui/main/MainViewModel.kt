package com.example.devicefinder.ui.main

import android.content.Context
import android.telephony.TelephonyManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import android.Manifest.permission.READ_PHONE_STATE
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.os.Message
import androidx.core.content.ContextCompat
import com.example.devicefinder.MainActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.logging.Handler
import kotlin.collections.HashMap


class MainViewModel(
    var context: Context?
) : ViewModel() {
    val showAlertMessage: MutableLiveData<String> = MutableLiveData()
    val requestPermission: MutableLiveData<String> = MutableLiveData()


    public val handler: android.os.Handler = object : android.os.Handler(Looper.getMainLooper()) {

        override fun handleMessage(inputMessage: Message) {
            val bundle: Bundle = inputMessage.data
            val key: String = "TEST"
            val s: String = bundle.getString(key.toString())?: key
            showAlertMessage.value = s
            Log.d("HELPER", "I made it here: $s")
        }
    }


    fun registerDevice(code: String) {
        Log.d("REGISTER_DEVICE", code)
        val imei = "1234"//getIMEINumber()

        if (imei == null) {
            showAlertMessage.value = "Error: Device IMEI number was unable to be retrieved. Check your permissions and try again."
            return
        }
        val params = getParams("12345", code)
        doAsync() {
            sendRegistrationPost(params)
        }.execute()
    }

    private fun getIMEINumber(): String? {
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
        Log.d("REGISTER_DEVICE", imei + " " + code)
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
        Log.d("REGISTER_DEVICE", "Sending Registration with " + params)
        var outputWriter: OutputStreamWriter? = null
        val url = "https://device-finder/api/v1/register"
        val msg = handler.obtainMessage()
        val bundle : Bundle = Bundle()
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
                    if (it.responseCode >= 200 && it.responseCode < 300) it.inputStream else it.errorStream
                }.let { streamToRead ->
                    BufferedReader(InputStreamReader(streamToRead)).use {
                        val response = StringBuffer()
                        Log.d("REGISTER_DEVICE", response.toString())

                        var inputLine = it.readLine()
                        while (inputLine != null) {
                            response.append(inputLine)
                            inputLine = it.readLine()
                        }
                        it.close()
                        response.toString()
                    }
                }
            Log.d("REGISTER_DEVICE", urlret)
        } catch (e: Exception) {

            bundle.putString("TEST","Your device could not be registered! Please try again." )
            msg.data = bundle
            handler.handleMessage(msg)
            //showAlertMessage.value = "Your device could not be registered! Please try again." // Possibly have boolean that we can check after the function gets to this point and then based on that set the value??
            Log.d("REGISTER_DEVICE", "Error: " + e.toString()) // Another possiblity is to call a function that is passed boolean that sets the toast
        } finally {
            bundle.putString("TEST", "Your device has been successfully registered.")
            msg.data = bundle
            handler.handleMessage(msg)

            outputWriter?.close()
        }
    }




}


