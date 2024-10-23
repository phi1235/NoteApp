package com.example.noteapp

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
        private const val TAG = "MainnActivity"
    }

    private lateinit var rvNotes: RecyclerView
    private lateinit var btnAddNote: ImageButton
    private lateinit var dbHelper: DatabaseHelper
    private var noteAdapter: NoteAdapter? = null
    private val notesList = mutableListOf<Pair<String, String>>()
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    // Xử lý khi người dùng chọn một mục trong menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // Xử lý đăng xuất
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
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

        // Cập nhật danh sách ghi chú ban đầu
        loadNotes()

        // Sự kiện click vào Menu
        ivMenu.setOnClickListener {
            Toast.makeText(this, "Menu Clicked", Toast.LENGTH_SHORT).show()
        }

        // Sự kiện click vào Avatar Profile
        ivProfile.setOnClickListener {
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
        }

        // Sự kiện click vào nút Thêm Ghi Chú
        btnAddNote.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivityForResult(intent, ADD_NOTE_REQUEST_CODE)
        }
        // Sự kiện click vào Dấu Ba Gạch (Menu)
        ivMenu.setOnClickListener {
            showPopupMenu(it)
        }
    }
    // Hàm để hiển thị PopupMenu khi nhấn vào ivMenu
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_main, popup.menu)

        // Xử lý sự kiện khi người dùng chọn mục trong PopupMenu
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_logout -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // Nhận kết quả từ AddNoteActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Sau khi ghi chú được thêm, tải lại danh sách ghi chú
            loadNotes()
        }
    }
    private fun logoutUser() {
        // Xóa thông tin đăng nhập trong SharedPreferences
        val sharedPref = getSharedPreferences("NoteAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()

        // Chuyển hướng về màn hình đăng nhập
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Đóng MainnActivity
    }
    private fun loadNotes() {
        // Lấy danh sách ghi chú từ cơ sở dữ liệu và cập nhật giao diện
        notesList.clear()
        notesList.addAll(dbHelper.getAllNotes())

        if (notesList.isEmpty()) {
            rvNotes.visibility = View.GONE
            Toast.makeText(this, "Chưa có ghi chú nào", Toast.LENGTH_SHORT).show()
        } else {
            rvNotes.visibility = View.VISIBLE

            // Thiết lập RecyclerView với NoteAdapter
            rvNotes.layoutManager =
                LinearLayoutManager(this) // Thiết lập LinearLayout cho RecyclerView
            rvNotes.adapter =
                NoteAdapter(notesList) // Sử dụng NoteAdapter để kết nối dữ liệu với giao diện
        }
        // Kiểm tra noteAdapter không bị null trước khi gọi notifyDataSetChanged
        noteAdapter?.notifyDataSetChanged()
        // In ra tiêu đề của từng ghi chú trong Logcat để kiểm tra
        for (note in notesList) {
            Log.d(TAG, "Note Title: ${note.first}, Content: ${note.second}")
        }
    }
    }
