package com.example.memoapp_firebase_practice

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoapp_firebase_practice.adapter.MemoAdapter
import com.example.memoapp_firebase_practice.model.Memo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MemoListActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var memoAdapter: MemoAdapter
    private val memoList = mutableListOf<Memo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_list)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        memoAdapter = MemoAdapter(memoList) { memo ->
            // 點擊 Memo 可導向編輯頁
            val intent = Intent(this, MemoEditActivity::class.java)
            intent.putExtra("memoId", memo.id)
            intent.putExtra("title", memo.title)
            intent.putExtra("content", memo.content)
            startActivity(intent)
        }
        recyclerView.adapter = memoAdapter

        // 點擊新增按鈕
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, MemoEditActivity::class.java))
        }

        loadMemos()
    }

    private fun loadMemos() {
        db.collection("memos")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                memoList.clear()
                for (doc in result) {
                    val memo = Memo(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                    memoList.add(memo)
                }
                memoAdapter.notifyDataSetChanged()
            }
    }
}
