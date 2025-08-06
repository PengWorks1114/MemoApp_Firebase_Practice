package com.example.memoapp_firebase_practice

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnDeleteAccount: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)

        // 🔐 帳號刪除邏輯
        btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("確認刪除帳號")
                .setMessage("此操作將永久刪除帳號與所有資料，是否繼續？")
                .setPositiveButton("刪除") { _, _ ->
                    FirebaseAuth.getInstance().currentUser?.delete()
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "帳號已刪除", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(this, "刪除失敗: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("取消", null)
                .show()
        }

        // 🌐 語言選單
        val languageList = listOf("中文", "日本語", "English")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languageList)
        spinnerLanguage.adapter = adapter

        // 👉 實作語言切換邏輯將於後續 Step 完成
    }
}
