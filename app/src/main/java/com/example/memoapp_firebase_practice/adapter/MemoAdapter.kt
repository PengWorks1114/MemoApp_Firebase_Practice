package com.example.memoapp_firebase_practice.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoapp_firebase_practice.R
import com.example.memoapp_firebase_practice.model.Memo

class MemoAdapter(
    private val items: List<Memo>,
    private val onItemClick: (Memo) -> Unit
) : RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

    inner class MemoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        val tvContent = itemView.findViewById<TextView>(R.id.tvContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memo, parent, false)
        return MemoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = items[position]
        holder.tvTitle.text = memo.title
        holder.tvContent.text = memo.content
        holder.itemView.setOnClickListener {
            onItemClick(memo)
        }
    }

    override fun getItemCount(): Int = items.size
}
