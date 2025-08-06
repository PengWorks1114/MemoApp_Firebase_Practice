package com.example.memoapp_firebase_practice

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // 顯示簡易 Logo（可之後改為圖片）
        val textView = findViewById<TextView>(R.id.textViewLogo)
        textView.text = "Memo App" // 初期以文字顯示

        // 模擬延遲 1 秒作為開場 Logo
        Handler(Looper.getMainLooper()).postDelayed({
            if (auth.currentUser != null) {
                // 已登入，跳到清單
                startActivity(Intent(this, MemoListActivity::class.java))
            } else {
                // 未登入，跳到登入
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 1000)
    }
}
