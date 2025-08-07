package com.example.memoapp_firebase_practice

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.onAttach(newBase)
        super.attachBaseContext(context)
    }

}
