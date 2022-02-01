package com.nrvz.prebuildapplication.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    open fun proceedToActivity(setClass: Class<*>, finish: Boolean = false) {
        val intent = Intent(this, setClass)
        startActivity(intent)
        if (finish) {
            finish()
        }
    }

    open fun proceedToActivity(intent: Intent, finish: Boolean = false) {
        startActivity(intent)
        if (finish) {
            finish()
        }
    }
}