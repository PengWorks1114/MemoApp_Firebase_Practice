package com.example.memoapp_firebase_practice

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : BaseActivity() {

    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnDeleteAccount: Button
    private var isFirstLanguageLoad = true // ✅ 避免 onItemSelected 初次觸發

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)

        // 🔐 Firebase 帳號刪除功能
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

        // 🌐 語言切換 Spinner 設定
        val languageList = listOf("中文", "日本語", "English")
        val languageCodes = listOf("zh", "ja", "en")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languageList)
        spinnerLanguage.adapter = adapter

        // 🔍 設定預設語言位置
        val currentLang = LocaleHelper.getCurrentLanguage(this)
        val defaultIndex = languageCodes.indexOf(currentLang).takeIf { it != -1 } ?: 0
        spinnerLanguage.setSelection(defaultIndex)

        // 🌀 語言選擇事件
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isFirstLanguageLoad) {
                    isFirstLanguageLoad = false
                    return
                }
                val selectedLang = languageCodes[position]
                if (selectedLang != LocaleHelper.getCurrentLanguage(this@SettingsActivity)) {
                    LocaleHelper.setLocale(this@SettingsActivity, selectedLang)
                    recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
