package com.example.devicefinder.ui.main

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject

class MainViewModel(
    var context: Context?
) : ViewModel() {
    val RequestQueue = Volley.newRequestQueue(context)
    val showAlertMessage: MutableLiveData<String> = MutableLiveData()

    fun registerDevice(code: String) {
        Log.d("REGISTER_DEVICE", code)
        val imei = getIMEINumber()
        val params = getParams(imei, code)
        sendRegistrationPostToServer(params)
    }

    private fun getIMEINumber(): String {
        // TODO: Retrieve IMEI
        return "This should be the imei number"
    }

    private fun getParams(imei: String, code: String): Map<String, String> {
        val deviceData: HashMap<String, String> = HashMap()
        deviceData["imei"] = imei
        deviceData["code"] = code
        return  deviceData
    }

    private fun sendRegistrationPostToServer(params: Map<String, String>) {
        // TODO: Add our server api path
        val url = "http://devicefinder.com/path-to-be-here"
        var registerDeviceRequest = StringRequest(Request.Method.POST, url,
            Response.Listener<String>() {
            @Override
            fun onResponse(response: String) {
                showAlertMessage.value = "Your device has been successfully registered."
                Log.d("REGISTER_DEVICE", "Response recieved: ${response.toString()}")
            }
        }
        , Response.ErrorListener() {
            @Override
            fun onErrorResponse(error: VolleyError) {
                showAlertMessage.value = "Your device could not be registered! Please try again." //This should eventually be more specifc to each error message
                Log.d("REGISTER_DEVICE", error.toString())
            }
        }).apply {
            @Override
            fun getBody(): ByteArray {
                return JSONObject(params).toString().toByteArray()
            }
        }
        RequestQueue.add(registerDeviceRequest);
    }
}
