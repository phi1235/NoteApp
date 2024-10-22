package com.example.noteapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp.DatabaseHelper  // Đảm bảo rằng dòng này được thêm vào

class RegisterActivity : AppCompatActivity() {

    // Khai báo đối tượng DatabaseHelper
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Khởi tạo đối tượng DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Kết nối với các thành phần UI
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // Sự kiện khi nhấn nút Đăng ký
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            } else {
                // Thêm người dùng vào cơ sở dữ liệu
                val success = dbHelper.addUser(email, password)
                if (success) {
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()

                    finish()  // Đóng Activity sau khi đăng ký thành công
                } else {
                    Toast.makeText(this, "Đăng ký thất bại, email có thể đã tồn tại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
