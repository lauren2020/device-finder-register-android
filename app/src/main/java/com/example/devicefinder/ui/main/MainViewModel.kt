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
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder


class MainViewModel(
    var context: Context?
) : ViewModel() {
    val RequestQueue = Volley.newRequestQueue(context)
    val showAlertMessage: MutableLiveData<String> = MutableLiveData()
    val requestPermission: MutableLiveData<String> = MutableLiveData()

    fun registerDevice(code: String) {
        Log.d("REGISTER_DEVICE", code)
        val imei = getIMEINumber()
        if (imei == null) {
            showAlertMessage.value = "Error: Device IMEI number was unable to be retrieved. Check your permissions and try again."
            return
        }
        val params = getParams(imei, code)
        doAsync {
            post(params)
        }.execute()
        //sendPostRequest(code, imei)
        //val params = getParams(imei, code)
        //sendRegistrationPostToServer(params)
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
        deviceData["imei"] = imei
        deviceData["code"] = code
        return  deviceData
    }

    private fun sendRegistrationPostToServer(params: Map<String, String>) {
        Log.d("REGISTER_DEVICE", "Sending request...")
        val url = "http://ec2-3-17-64-157.us-east-2.compute.amazonaws.com/api/v1/register"
        var registerDeviceRequest = StringRequest(Request.Method.POST, url,
            Response.Listener<String>() {
            @Override
            fun onResponse(response: String) {
                showAlertMessage.value = "Your device has been successfully registered."
                Log.d("REGISTER_DEVICE", "Success Response recieved: ${response.toString()}")
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
                //JSONObject(params).toString()
                Log.d("REGISTER_DEVICE", "Overriding Body: " + JSONObject(params).toString())
                return JSONObject(params).toString().toByteArray()
            }
        }
        RequestQueue.add(registerDeviceRequest)
    }

    fun sendPostRequest(code: String, imei: String) {

        var reqParam = URLEncoder.encode("code", "UTF-8") + "=" + URLEncoder.encode(code, "UTF-8")
        reqParam += "&" + URLEncoder.encode("imei", "UTF-8") + "=" + URLEncoder.encode(imei, "UTF-8")
        val mURL = URL("http://ec2-3-17-64-157.us-east-2.compute.amazonaws.com/api/v1/register")

        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "POST"

            val wr = OutputStreamWriter(getOutputStream());
            wr.write(reqParam);
            wr.flush();

            println("URL : $url")
            println("Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                println("Response : $response")
            }
        }
    }

//    fun post(params: Map<String, String>): String {
//        val url = "http://ec2-3-17-64-157.us-east-2.compute.amazonaws.com/api/v1/register"
//        return URL(url)
//            .openConnection()
//            .let {
//                it as HttpURLConnection
//            }.apply {
//                setRequestProperty("Content-Type", "application/json; charset=utf-8")
//                requestMethod = "POST"
//
//                doOutput = true
//                val outputWriter = OutputStreamWriter(outputStream)
//                outputWriter.write(JSONObject(params).toString())
//                outputWriter.flush()
//            }.let {
//                if (it.responseCode == 200) it.inputStream else it.errorStream
//            }.let { streamToRead ->
//                BufferedReader(InputStreamReader(streamToRead)).use {
//                    val response = StringBuffer()
//
//                    var inputLine = it.readLine()
//                    while (inputLine != null) {
//                        response.append(inputLine)
//                        inputLine = it.readLine()
//                    }
//                    it.close()
//                    response.toString()
//                }
//            }
//    }

    private fun post(params: Map<String, String>) {
        var outputWriter: OutputStreamWriter? = null
        val url = "http://ec2-3-17-64-157.us-east-2.compute.amazonaws.com/api/v1/register"
        try {
            val urlret = URL(url)
                .openConnection()
                .let {
                    it as HttpURLConnection
                }.apply {
                    Log.d("REGISTER_DEVICE", "in apply")
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    requestMethod = "POST"

                    doOutput = true
                    outputWriter = OutputStreamWriter(outputStream)
                    outputWriter?.write(JSONObject(params).toString())
                    outputWriter?.flush()
                }.let {
                    if (it.responseCode == 200) it.inputStream else it.errorStream
                }.let { streamToRead ->
                    BufferedReader(InputStreamReader(streamToRead)).use {
                        Log.d("REGISTER_DEVICE", "In buffer reader")
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
            //showAlertMessage.value = "Your device could not be registered! Please try again."
            Log.d("REGISTER_DEVICE", "Error: " + e.toString())
        } finally {
            //showAlertMessage.value = "Your device has been successfully registered."
            outputWriter?.close()
        }
    }
}
