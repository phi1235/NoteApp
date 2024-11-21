package com.example.noteapp

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val notes: List<Triple<Int, String, String>>,
    private val onNoteClick: (Int) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        init {
            // Gọi hàm callback khi nhấn vào item
            itemView.setOnClickListener {
                onNoteClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val (noteId, title, content) = notes[position]
        holder.tvTitle.text = title
        holder.tvContent.text = content
        // Kiểm tra độ dài nội dung để điều chỉnh số dòng tối đa
        if (content.length > 60) {
            holder.tvContent.maxLines = 6  // Giới hạn tối đa số dòng
            holder.tvContent.ellipsize = TextUtils.TruncateAt.END  // Thêm "..." nếu nội dung quá dài
        } else {
            holder.tvContent.maxLines = Int.MAX_VALUE  // Không giới hạn số dòng nếu nội dung ngắn
            holder.tvContent.ellipsize = null  // Không thêm "..."
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }
}
