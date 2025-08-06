package com.example.memoapp_firebase_practice

import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
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
            val intent = Intent(this, MemoEditActivity::class.java)
            intent.putExtra("memoId", memo.id)
            intent.putExtra("title", memo.title)
            intent.putExtra("content", memo.content)
            startActivity(intent)
        }
        recyclerView.adapter = memoAdapter

        // 設定左滑刪除 + 確認視窗
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val memo = memoList[position]

                // 顯示確認對話框
                AlertDialog.Builder(this@MemoListActivity)
                    .setTitle("確認刪除")
                    .setMessage("是否確定要刪除這筆記事？")
                    .setPositiveButton("刪除") { _, _ ->
                        db.collection("memos").document(memo.id)
                            .delete()
                            .addOnSuccessListener {
                                memoList.removeAt(position)
                                memoAdapter.notifyItemRemoved(position)
                                Toast.makeText(this@MemoListActivity, "刪除成功", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@MemoListActivity, "刪除失敗: ${it.message}", Toast.LENGTH_SHORT).show()
                                memoAdapter.notifyItemChanged(position)
                            }
                    }
                    .setNegativeButton("取消") { _, _ ->
                        memoAdapter.notifyItemChanged(position)
                    }
                    .setCancelable(false)
                    .show()
            }

            // 繪製紅色背景與刪除圖示
            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                val itemView = viewHolder.itemView
                val paint = Paint()
                paint.color = Color.RED

                c.drawRect(
                    itemView.right.toFloat() + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat(),
                    paint
                )

                val icon = ContextCompat.getDrawable(this@MemoListActivity, android.R.drawable.ic_menu_delete)
                icon?.let {
                    val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                    val iconTop = itemView.top + iconMargin
                    val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconBottom = iconTop + it.intrinsicHeight

                    it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    it.draw(c)
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)

        // 點擊新增按鈕
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, MemoEditActivity::class.java))
        }

        loadMemos()
    }

    override fun onResume() {
        super.onResume()
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
