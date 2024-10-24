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
        private const val DATABASE_NAME = "NoteApp.db"
        private const val DATABASE_VERSION = 3 // Cập nhật phiên bản cơ sở dữ liệu

        // Tên bảng và cột cho bảng user
        const val TABLE_USER = "user"
        const val COLUMN_ID = "id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"

        // Tên bảng và cột cho bảng notes
        const val TABLE_NOTE = "notes"
        const val COLUMN_NOTE_ID = "note_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_USER_ID = "user_id" // Cột mới cho user_id

        // Câu lệnh tạo bảng user
        private const val CREATE_TABLE_USER = ("CREATE TABLE $TABLE_USER ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_EMAIL TEXT, "
                + "$COLUMN_PASSWORD TEXT)")

        // Câu lệnh tạo bảng ghi chú với user_id
        private const val CREATE_TABLE_NOTE = ("CREATE TABLE IF NOT EXISTS $TABLE_NOTE ("
                + "$COLUMN_NOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_TITLE TEXT, "
                + "$COLUMN_CONTENT TEXT, "
                + "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "$COLUMN_USER_ID INTEGER, " // Liên kết với bảng user
                + "FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USER($COLUMN_ID) ON DELETE CASCADE)")
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(CREATE_TABLE_USER)  // Tạo bảng user
            db.execSQL(CREATE_TABLE_NOTE)  // Tạo bảng notes
        } catch (e: Exception) {
            Log.e(TAG, "Error creating table: ${e.message}")
        }
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
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
    // Hàm lấy ID người dùng dựa trên email
    fun getUserIdByEmail(email: String): Int? {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        return try {
            cursor = db.query(
                TABLE_USER,
                arrayOf(COLUMN_ID),
                "$COLUMN_EMAIL = ?",
                arrayOf(email),
                null,
                null,
                null
            )
            if (cursor.moveToFirst()) {
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            cursor?.close()
            db.close()
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
    fun addNote(title: String, content: String, userId: Int): Boolean {
        var db: SQLiteDatabase? = null
        return try {
            db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_TITLE, title)
                put(COLUMN_CONTENT, content)
                put(COLUMN_USER_ID, userId) // Lưu user_id vào bảng notes
            }
            val result = db.insert(TABLE_NOTE, null, values)
            result != -1L
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting note: ${e.message}")
            false
        } finally {
            db?.close()
        }
    }


    // Hàm lấy tất cả các ghi chú của người dùng từ bảng notes - phan quyen account
    fun getAllNotes(userId: Int): List<Triple<Int, String, String>> {
        val notes = mutableListOf<Triple<Int, String, String>>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        return try {
            cursor = db.rawQuery(
                "SELECT * FROM $TABLE_NOTE WHERE $COLUMN_USER_ID = ? ORDER BY $COLUMN_TIMESTAMP DESC",
                arrayOf(userId.toString())
            )
            if (cursor.moveToFirst()) {
                do {
                    val noteId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                    val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                    notes.add(Triple(noteId, title, content))
                } while (cursor.moveToNext())
            }
            notes
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Trả về danh sách rỗng nếu có lỗi
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun getNoteById(noteId: Int): Pair<String, String>? {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        return try {
            cursor = db.query(
                TABLE_NOTE,
                arrayOf(COLUMN_TITLE, COLUMN_CONTENT), // Lấy tiêu đề và nội dung
                "$COLUMN_NOTE_ID = ?", // Điều kiện truy vấn
                arrayOf(noteId.toString()), // Giá trị của điều kiện
                null,
                null,
                null
            )
            if (cursor.moveToFirst()) {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                Pair(title, content)  // Trả về tiêu đề và nội dung
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting note: ${e.message}")
            null
        } finally {
            cursor?.close()
            db.close()
        }
    }


    fun updateNote(noteId: Int, title: String, content: String): Boolean {
        var db: SQLiteDatabase? = null
        return try {
            db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_TITLE, title)
                put(COLUMN_CONTENT, content)
            }
            val result = db.update(TABLE_NOTE, values, "$COLUMN_NOTE_ID = ?", arrayOf(noteId.toString()))
            result > 0  // Trả về true nếu cập nhật thành công
        } catch (e: Exception) {
            Log.e(TAG, "Error updating note: ${e.message}")
            false
        } finally {
            db?.close()
        }
    }
    fun deleteNoteById(noteId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NOTE, "$COLUMN_NOTE_ID = ?", arrayOf(noteId.toString()))
        db.close()
    }


}
