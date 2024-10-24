package com.example.noteapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainnActivity : AppCompatActivity() {

    companion object {
        const val ADD_NOTE_REQUEST_CODE = 1
        const val EDIT_NOTE_REQUEST_CODE = 2  // Định nghĩa hằng số cho mã yêu cầu chỉnh sửa
        private const val TAG = "MainnActivity"
    }

    private lateinit var rvNotes: RecyclerView
    private lateinit var btnAddNote: ImageButton
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var noteAdapter: NoteAdapter
    private val notesList = mutableListOf<Triple<Int, String, String>>() // Danh sách ghi chú, thêm note_id vào Triple

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainn)

        // Kết nối với các thành phần UI
        val ivMenu = findViewById<ImageView>(R.id.ivMenu)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        rvNotes = findViewById(R.id.rvNotes)
        btnAddNote = findViewById(R.id.btnAddNote)

        // Khởi tạo DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Khởi tạo Adapter với danh sách rỗng
        noteAdapter = NoteAdapter(notesList) { position ->
            // Xử lý khi người dùng nhấn vào ghi chú
            val noteId = notesList[position].first
            val intent = Intent(this, EditNoteActivity::class.java).apply {
                putExtra("note_id", noteId)  // Truyền note_id vào Intent
            }
            startActivityForResult(intent, EDIT_NOTE_REQUEST_CODE)
        }

        // Thiết lập RecyclerView với NoteAdapter và LayoutManager
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = noteAdapter

        // Cập nhật danh sách ghi chú ban đầu
        loadNotes()
        // Sự kiện click vào Menu (hiển thị PopupMenu với đăng xuất)
        ivMenu.setOnClickListener {
            showPopupMenu(it)
        }

        // Sự kiện click vào nút Thêm Ghi Chú
        btnAddNote.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivityForResult(intent, ADD_NOTE_REQUEST_CODE)
        }
    }
    // Hàm để hiển thị PopupMenu khi nhấn vào nút ba gạch
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)

        // Xử lý sự kiện khi người dùng chọn mục trong PopupMenu
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    // Nhận kết quả từ AddNoteActivity hoặc EditNoteActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            loadNotes()  // Tải lại danh sách ghi chú sau khi thêm
        }
        if (requestCode == EDIT_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            loadNotes()  // Tải lại danh sách ghi chú sau khi chỉnh sửa va xoa
        }
    }
// load note ve man hinh main sau khi add-edit-delete
    private fun loadNotes() {
        // Lấy user_id từ SharedPreferences
        val sharedPref = getSharedPreferences("NoteAppPreferences", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Không thể xác định người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy danh sách ghi chú từ cơ sở dữ liệu và cập nhật giao diện
        notesList.clear()
        notesList.addAll(dbHelper.getAllNotes(userId)) // Lấy ghi chú của user hiện tại

        if (notesList.isEmpty()) {
            rvNotes.visibility = View.GONE
            Toast.makeText(this, "Chưa có ghi chú nào", Toast.LENGTH_SHORT).show()
        } else {
            rvNotes.visibility = View.VISIBLE
            noteAdapter.notifyDataSetChanged()
        }
    }
    // fun logout account
    private fun logoutUser() {
        // Xóa thông tin đăng nhập trong SharedPreferences
        val sharedPref = getSharedPreferences("NoteAppPreferences", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()  // Xóa hết các giá trị đã lưu
        editor.apply()

        // Chuyển hướng về màn hình đăng nhập
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Đóng MainnActivity
    }

}
