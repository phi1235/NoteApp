package com.example.noteapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    // Khai báo đối tượng DatabaseHelper
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etEmail:EditText
    private lateinit var etPassword:EditText
    private lateinit var btnLogin:Button
    private lateinit var tvForgotPassword:TextView
    private lateinit var tvCreateAccount:TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setControl()
        setEvent()
    }

    private fun setEvent() {
        // Khởi tạo đối tượng DatabaseHelper
        dbHelper = DatabaseHelper(this)
        // Sự kiện nút Đăng nhập
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        // Kiểm tra trạng thái đăng nhập
        val sharedPref = getSharedPreferences("NoteAppPreferences", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            // Nếu đã đăng nhập, chuyển hướng sang MainnActivity
            navigateToMain()
            return
        }
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            } else {
                val isUserExists = dbHelper.checkUser(email, password)
                if (isUserExists) {
                    val userId = dbHelper.getUserIdByEmail(email) // Lấy user_id của người dùng

                    if (userId != null) {

                        val editor = sharedPref.edit()
                        editor.putBoolean("isLoggedIn", true)
                        editor.putInt("user_id", userId) // Lưu user_id vào SharedPreferences
                        editor.apply()

                        navigateToMain() // Chuyển sang MainActivity
                    } else {
                        Toast.makeText(this, "Không thể tìm thấy userId", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT)
                        .show()
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

    private fun setControl() {
        // Kết nối với các thành phần UI
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvCreateAccount = findViewById(R.id.tvCreateAccount)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainnActivity::class.java)
        startActivity(intent)
        finish() // Đóng LoginActivity để ngăn quay lại màn hình đăng nhập
    }
}
