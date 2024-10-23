package com.example.noteapp

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddNoteActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var ivSave: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var dbHelper: DatabaseHelper

    companion object {
        private const val TAG = "AddNoteActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        // Kết nối với các thành phần UI
        ivBack = findViewById(R.id.ivBack)
        ivSave = findViewById(R.id.ivSave)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)

        // Khởi tạo DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Xử lý sự kiện click vào nút quay lại
        ivBack.setOnClickListener {
            saveNote()
            finish() // Quay lại màn hình trước đó
        }

        // Xử lý sự kiện click vào nút lưu
        ivSave.setOnClickListener {
            saveNote()

            // Gửi kết quả về cho MainActivity để cập nhật danh sách ghi chú
            setResult(RESULT_OK)
            finish()
        }

    }

    private fun saveNote() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        // Kiểm tra nếu cả tiêu đề và nội dung đều trống thì không lưu
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Không có nội dung để lưu", Toast.LENGTH_SHORT).show()
            return
        }

        // Lưu ghi chú vào cơ sở dữ liệu
        val success = dbHelper.addNote(title, content)
        if (success) {
            Toast.makeText(this, "Đã lưu ghi chú", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Lưu ghi chú thất bại", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Failed to save note with title: $title, content: $content")
        }
    }
}
