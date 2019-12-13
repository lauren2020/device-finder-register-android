package com.example.devicefinder.ui.main

import android.Manifest
import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.devicefinder.R
import kotlinx.android.synthetic.main.main_fragment.view.*
import androidx.core.app.ActivityCompat
import android.widget.Toast
import com.example.devicefinder.MainActivity


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()

//        public val handler: Handler = object : Handler(Looper.getMainLooper()) {
//
//             override fun handleMessage(inputMessage: Message) {
//                 val bundle: Bundle = inputMessage.data
//                 val key: String = "TEST"
//                 val s: String = bundle.getString(key.toString())?: key
//                 showAlertMessage(s)
//                Log.d("HELPER", "I made it here: $s")
//                }
//            }


    }

    private lateinit var viewModel: MainViewModel




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, MainViewModelFactory(context)).get(MainViewModel::class.java)
    }

    fun handleMessage(inputMessage: Message) {
        val s = inputMessage.toString()
        //MainFragment.showAlertMessage(s)
        Log.d("HELPER", "I made it here: $inputMessage")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.registerDeviceButton.setOnClickListener {
            viewModel.registerDevice(view.codeEntryEditText.text.toString())
        }
        viewModel.showAlertMessage.observe(this, Observer {
            showAlertMessage(it)
        })
        viewModel.requestPermission.observe(this, Observer {
            if (activity != null) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(it),
                    1
                )
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProviders.of(this, MainViewModelFactory(context)).get(MainViewModel::class.java)
    }


    private fun showAlertMessage(message: String) {
        // TODO: Show Alert
        val unwrappedContext = context ?: return
        Toast.makeText(unwrappedContext, message,
            Toast.LENGTH_LONG).show()
    }
}



