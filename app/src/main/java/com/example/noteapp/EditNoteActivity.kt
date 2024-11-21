package com.example.noteapp
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class EditNoteActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2
    private lateinit var ivAddImage: ImageView
    private lateinit var ivBackEdit: ImageView
    private lateinit var ivSaveEdit: ImageView
    private lateinit var ivMenu: ImageView
    private lateinit var etEditTitle: EditText
    private lateinit var etEditContent: EditText
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var ivSelectedImage: ImageView
    private var noteId: Int = -1  // Biến để lưu id của ghi chú

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        setControl()
        setEvent()



    }
    private fun openImagePickerDialog() {
        val options = arrayOf("Chọn ảnh từ thư viện", "Chụp ảnh")

        // Sử dụng Dialog hoặc đơn giản là chọn tùy chọn từ một AlertDialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Chọn phương thức thêm ảnh")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Chọn ảnh từ thư viện
                    openGallery()
                }
                1 -> {
                    // Chụp ảnh
                    openCamera()
                }
            }
        }
        builder.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {
        // Kiểm tra quyền camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
        } else {
            // Yêu cầu quyền camera nếu chưa có
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAPTURE_IMAGE_REQUEST)
        }
    }

    // Xử lý kết quả trả về từ Intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val selectedImageUri: Uri? = data?.data
                    ivSelectedImage.setImageURI(selectedImageUri)
                    ivSelectedImage.visibility = ImageView.VISIBLE
                }
                CAPTURE_IMAGE_REQUEST -> {
                    val photo: Bitmap = data?.extras?.get("data") as Bitmap
                    ivSelectedImage.setImageBitmap(photo)
                    ivSelectedImage.visibility = ImageView.VISIBLE
                }
            }
            // Đẩy tiêu đề và nội dung xuống dưới ảnh
            etEditTitle.setPadding(0, 0, 0, 200)  // Điều chỉnh padding để không bị che
            etEditContent.setPadding(0, 0, 0, 200)
        }
    }

    // Xử lý kết quả yêu cầu quyền camera
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAPTURE_IMAGE_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Cần quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setEvent() {
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
        ivAddImage.setOnClickListener {
            // Mở dialog để chọn phương thức
            openImagePickerDialog()
        }
    }

    private fun setControl() {
        ivBackEdit = findViewById(R.id.ivBackEdit)
        ivSaveEdit = findViewById(R.id.ivSaveEdit)
        ivMenu = findViewById(R.id.ivMenu)
        etEditTitle = findViewById(R.id.etEditTitle)
        etEditContent = findViewById(R.id.etEditContent)
        dbHelper = DatabaseHelper(this)
        ivAddImage = findViewById(R.id.ivAddImage)
        ivSelectedImage = findViewById(R.id.ivSelectedImage)
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
