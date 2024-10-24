package com.example.noteapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditNoteActivity : AppCompatActivity() {

    private lateinit var ivBackEdit: ImageView
    private lateinit var ivSaveEdit: ImageView
    private lateinit var ivMenu: ImageView
    private lateinit var etEditTitle: EditText
    private lateinit var etEditContent: EditText
    private lateinit var dbHelper: DatabaseHelper
    private var noteId: Int = -1  // Biến để lưu id của ghi chú

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        ivBackEdit = findViewById(R.id.ivBackEdit)
        ivSaveEdit = findViewById(R.id.ivSaveEdit)
        ivMenu = findViewById(R.id.ivMenu)
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
        // Bổ sung menu Popup
        ivMenu.setOnClickListener {
            showPopupMenu(ivMenu)
        }
    }
    // Hàm để hiển thị PopupMenu
    private fun showPopupMenu(view: ImageView) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_options, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete -> {
                    // Gọi hàm xóa ghi chú
                    deleteNote()
                    true
                }
                R.id.menu_share -> {
                    // Xử lý sự kiện chia sẻ ghi chú
                    shareNote()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    // fun shareNote
    private fun shareNote() {
        val title = etEditTitle.text.toString().trim()
        val content = etEditContent.text.toString().trim()

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Không có nội dung để chia sẻ", Toast.LENGTH_SHORT).show()
            return
        }

        // Nội dung chia sẻ
        val shareText = "Tiêu đề: $title\nNội dung: $content"

        // Intent để chia sẻ nội dung
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        // Mở giao diện chọn ứng dụng để chia sẻ
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ ghi chú qua:"))
    }

    private fun deleteNote() {
        if (noteId != -1) {
            dbHelper.deleteNoteById(noteId) // Xóa ghi chú khỏi cơ sở dữ liệu bằng ID
            Toast.makeText(this, "Ghi chú đã bị xóa", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)  // Để thông báo cho MainActivity cập nhật lại danh sách
            finish()  // Đóng màn hình EditNoteActivity và quay lại màn hình trước
        } else {
            Toast.makeText(this, "Không thể xóa ghi chú", Toast.LENGTH_SHORT).show()
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
