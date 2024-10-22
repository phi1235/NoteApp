package com.example.noteapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp.DatabaseHelper

class LoginActivity : AppCompatActivity() {

    // Khai báo đối tượng DatabaseHelper
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Khởi tạo đối tượng DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Kết nối với các thành phần UI
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvCreateAccount = findViewById<TextView>(R.id.tvCreateAccount)

        // Sự kiện nút Đăng nhập
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            } else {
                // Kiểm tra email và mật khẩu trong cơ sở dữ liệu
                val isUserExists = dbHelper.checkUser(email, password)
                if (isUserExists) {
                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Sự kiện "Tạo tài khoản"
        tvCreateAccount.setOnClickListener {
            // Tạo Intent để chuyển từ LoginActivity sang RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)  // Bắt đầu RegisterActivity
        }
// In thông tin người dùng đã đăng ký ra Logcat
        val users = dbHelper.getAllUserDetails()
        for ((userEmail, userPassword) in users) {
            Log.d("USER_INFO", "Email: $userEmail, Password: $userPassword")
        }
        // Sự kiện "Quên mật khẩu?"
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Chức năng Quên mật khẩu chưa được phát triển", Toast.LENGTH_SHORT).show()
        }
    }
}
