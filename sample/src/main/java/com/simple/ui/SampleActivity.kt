package com.simple.ui

import android.os.Bundle
import com.okay.sampletamplate.SampleAppCompatActivity
import com.simple.R

class SampleActivity : SampleAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_view)
    }
}
