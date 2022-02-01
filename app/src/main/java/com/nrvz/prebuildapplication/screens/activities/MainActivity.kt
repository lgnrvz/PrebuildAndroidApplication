package com.nrvz.prebuildapplication.screens.activities

import android.os.Bundle
import com.nrvz.prebuildapplication.R
import com.nrvz.prebuildapplication.screens.BaseActivity

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}