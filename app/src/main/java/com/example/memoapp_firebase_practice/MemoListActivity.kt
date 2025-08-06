package com.example.memoapp_firebase_practice

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.SearchView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoapp_firebase_practice.adapter.MemoAdapter
import com.example.memoapp_firebase_practice.model.Memo
import com.example.memoapp_firebase_practice.model.SortType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MemoListActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var memoAdapter: MemoAdapter
    private lateinit var searchView: SearchView
    private lateinit var sortSpinner: Spinner

    private val memoList = mutableListOf<Memo>()       // 所有資料
    private val filteredList = mutableListOf<Memo>()   // 顯示用資料

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_list)

        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        memoAdapter = MemoAdapter(filteredList,
            onItemClick = { memo ->
                val intent = Intent(this, MemoEditActivity::class.java)
                intent.putExtra("memoId", memo.id)
                intent.putExtra("title", memo.title)
                intent.putExtra("content", memo.content)
                startActivity(intent)
            },
            onFavoriteClick = { memo ->
                val newFavorite = !memo.favorite
                db.collection("memos").document(memo.id)
                    .update("favorite", newFavorite)
                    .addOnSuccessListener {
                        memo.favorite = newFavorite
                        memoAdapter.notifyDataSetChanged()
                        Toast.makeText(this,
                            if (newFavorite) "已加入最愛" else "已移除最愛", Toast.LENGTH_SHORT).show()
                    }
            }
        )
        recyclerView.adapter = memoAdapter

        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterMemoList(newText ?: "")
                return true
            }
        })

        sortSpinner = findViewById(R.id.spinnerSort)
        val sortOptions = arrayOf("時間：新 → 舊", "時間：舊 → 新", "標題 A→Z", "最愛優先")
        sortSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        sortSpinner.setSelection(0)
        sortSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                val selectedSort = when (position) {
                    0 -> SortType.TIME_DESC
                    1 -> SortType.TIME_ASC
                    2 -> SortType.TITLE_ASC
                    3 -> SortType.FAVORITE
                    else -> SortType.TIME_DESC
                }
                sortMemoList(selectedSort)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val memo = filteredList[position]

                AlertDialog.Builder(this@MemoListActivity)
                    .setTitle("確認刪除")
                    .setMessage("是否確定要刪除這筆記事？")
                    .setPositiveButton("刪除") { _, _ ->
                        db.collection("memos").document(memo.id)
                            .delete()
                            .addOnSuccessListener {
                                memoList.removeIf { it.id == memo.id }
                                filterMemoList(searchView.query.toString())
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

            override fun onChildDraw(c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder, dX: Float, dY: Float, state: Int, isActive: Boolean) {
                super.onChildDraw(c, rv, vh, dX, dY, state, isActive)
                val itemView = vh.itemView
                val paint = Paint().apply { color = Color.RED }

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
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        favorite = doc.getBoolean("favorite") ?: false
                    )
                    memoList.add(memo)
                }
                filterMemoList(searchView.query.toString())
            }
    }

    private fun filterMemoList(keyword: String) {
        filteredList.clear()
        if (keyword.isEmpty()) {
            filteredList.addAll(memoList)
        } else {
            for (memo in memoList) {
                if (memo.title.contains(keyword, true) || memo.content.contains(keyword, true)) {
                    filteredList.add(memo)
                }
            }
        }
        memoAdapter.notifyDataSetChanged()
    }

    private fun sortMemoList(type: SortType) {
        when (type) {
            SortType.TIME_DESC -> filteredList.sortByDescending { it.timestamp }
            SortType.TIME_ASC -> filteredList.sortBy { it.timestamp }
            SortType.TITLE_ASC -> filteredList.sortBy { it.title }
            SortType.FAVORITE -> filteredList.sortWith(compareByDescending<Memo> { it.favorite }.thenByDescending { it.timestamp })
        }
        memoAdapter.notifyDataSetChanged()
    }
}
