package com.example.memoapp_firebase_practice

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MemoEditActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var memoId: String? = null // 若為 null 則代表新增

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_edit)

        db = FirebaseFirestore.getInstance()

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etContent = findViewById<EditText>(R.id.etContent)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // 接收傳入資料
        memoId = intent.getStringExtra("memoId")
        etTitle.setText(intent.getStringExtra("title") ?: "")
        etContent.setText(intent.getStringExtra("content") ?: "")

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "請輸入標題與內容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val memoData = hashMapOf(
                "title" to title,
                "content" to content,
                "timestamp" to Date().time
            )

            if (memoId == null) {
                // 新增
                db.collection("memos")
                    .add(memoData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "新增成功", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "新增失敗: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // 更新
                db.collection("memos").document(memoId!!)
                    .set(memoData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "更新失敗: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
