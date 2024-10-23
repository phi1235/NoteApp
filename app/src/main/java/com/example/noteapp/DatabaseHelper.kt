package com.example.noteapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.database.Cursor
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NoteApp.db"  // Tên file cơ sở dữ liệu
        private const val DATABASE_VERSION = 2  // Phiên bản của cơ sở dữ liệu

        // Tên bảng và cột cho bảng user
        const val TABLE_USER = "user"
        const val COLUMN_ID = "id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"

        // Tên bảng và cột cho bảng ghi chú
        const val TABLE_NOTE = "notes"
        const val COLUMN_NOTE_ID = "note_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_TIMESTAMP = "timestamp"

        // Câu lệnh tạo bảng user
        private const val CREATE_TABLE_USER = ("CREATE TABLE $TABLE_USER ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_EMAIL TEXT, "
                + "$COLUMN_PASSWORD TEXT)")

        // Câu lệnh tạo bảng ghi chú
        private const val CREATE_TABLE_NOTE = ("CREATE TABLE IF NOT EXISTS $TABLE_NOTE ("
                + "$COLUMN_NOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_TITLE TEXT, "
                + "$COLUMN_CONTENT TEXT, "
                + "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)")

        private const val TAG = "DatabaseHelper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(CREATE_TABLE_NOTE)  // Tạo bảng notes
        } catch (e: Exception) {
            Log.e(TAG, "Error creating table: ${e.message}")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTE")
            onCreate(db)
        } catch (e: Exception) {
            Log.e(TAG, "Error upgrading table: ${e.message}")
        }
    }

    // Hàm thêm người dùng mới vào bảng user
    fun addUser(email: String, password: String): Boolean {
        var db: SQLiteDatabase? = null
        return try {
            db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_EMAIL, email)
                put(COLUMN_PASSWORD, password)
            }
            val result = db.insert(TABLE_USER, null, values)
            result != -1L // Trả về true nếu thêm thành công
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db?.close()
        }
    }

    // Hàm lấy tất cả thông tin người dùng (email và mật khẩu)
    fun getAllUserDetails(): List<Pair<String, String>> {
        val userDetails = mutableListOf<Pair<String, String>>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        return try {
            cursor = db.rawQuery("SELECT * FROM $TABLE_USER", null)
            if (cursor.moveToFirst()) {
                do {
                    val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                    val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
                    userDetails.add(Pair(email, password))
                } while (cursor.moveToNext())
            }
            userDetails
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()  // Trả về danh sách rỗng nếu có lỗi
        } finally {
            cursor?.close()
            db.close()
        }
    }

    // Hàm kiểm tra đăng nhập của người dùng
    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        return try {
            cursor = db.query(
                TABLE_USER,
                arrayOf(COLUMN_ID),  // Chỉ cần lấy cột ID để kiểm tra tồn tại
                "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",  // Điều kiện truy vấn
                arrayOf(email, password),  // Giá trị của email và password
                null,
                null,
                null
            )
            cursor.count > 0  // Trả về true nếu có ít nhất một kết quả khớp
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            cursor?.close()
            db.close()
        }
    }

    // Hàm thêm ghi chú mới vào bảng notes
    fun addNote(title: String, content: String): Boolean {
        var db: SQLiteDatabase? = null
        return try {
            db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_TITLE, title)
                put(COLUMN_CONTENT, content)
            }
            val result = db.insert(TABLE_NOTE, null, values)
            if (result == -1L) {
                Log.e(TAG, "Failed to insert note into database.")
            }
            result != -1L // Trả về true nếu thêm thành công
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting note: ${e.message}")
            false
        } finally {
            db?.close()
        }
    }

    // Hàm lấy tất cả các ghi chú từ bảng notes
    fun getAllNotes(): List<Pair<String, String>> {
        val notes = mutableListOf<Pair<String, String>>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        return try {
            cursor = db.rawQuery("SELECT * FROM $TABLE_NOTE ORDER BY $COLUMN_TIMESTAMP DESC", null)
            if (cursor.moveToFirst()) {
                do {
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                    val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                    notes.add(Pair(title, content))
                } while (cursor.moveToNext())
            }
            notes
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()  // Trả về danh sách rỗng nếu có lỗi
        } finally {
            cursor?.close()
            db.close()
        }
    }
}
