package com.example.noteapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditNoteActivity : AppCompatActivity() {

    private lateinit var ivBackEdit: ImageView
    private lateinit var ivSaveEdit: ImageView
    private lateinit var etEditTitle: EditText
    private lateinit var etEditContent: EditText
    private lateinit var dbHelper: DatabaseHelper
    private var noteId: Int = -1  // Biến để lưu id của ghi chú

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        ivBackEdit = findViewById(R.id.ivBackEdit)
        ivSaveEdit = findViewById(R.id.ivSaveEdit)
        etEditTitle = findViewById(R.id.etEditTitle)
        etEditContent = findViewById(R.id.etEditContent)

        dbHelper = DatabaseHelper(this)

        // Nhận note_id từ Intent
        noteId = intent.getIntExtra("note_id", -1)

        if (noteId != -1) {
            loadNoteData(noteId)  // Nếu có note_id, lấy dữ liệu từ CSDL
        } else {
            Toast.makeText(this, "Không tìm thấy ghi chú", Toast.LENGTH_SHORT).show()
        }

        ivBackEdit.setOnClickListener {
            saveOrUpdateNote()
            setResult(RESULT_OK)
            finish()
        }

        ivSaveEdit.setOnClickListener {
            saveOrUpdateNote()
            setResult(RESULT_OK)
            finish()
        }
    }

    // Hàm để tải thông tin ghi chú từ CSDL
    private fun loadNoteData(noteId: Int) {
        val note = dbHelper.getNoteById(noteId)
        if (note != null) {
            etEditTitle.setText(note.first)
            etEditContent.setText(note.second)
        } else {
            Toast.makeText(this, "Không tìm thấy ghi chú", Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm để lưu hoặc cập nhật ghi chú
    private fun saveOrUpdateNote() {
        val title = etEditTitle.text.toString().trim()
        val content = etEditContent.text.toString().trim()

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Không có nội dung để lưu", Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy user_id từ SharedPreferences
        val sharedPref = getSharedPreferences("NoteAppPreferences", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Không thể xác định người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        if (noteId == -1) {
            dbHelper.addNote(title, content, userId)  // Thêm ghi chú mới
        } else {

            dbHelper.updateNote(noteId, title, content)  // Cập nhật ghi chú
            Toast.makeText(this, "Cập nhật ghi chú thành công", Toast.LENGTH_SHORT).show()

        }
    }
}
