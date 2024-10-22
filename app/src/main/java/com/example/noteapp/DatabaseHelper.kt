import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NoteApp.db"  // Tên file cơ sở dữ liệu
        private const val DATABASE_VERSION = 1  // Phiên bản của cơ sở dữ liệu

        // Tên bảng và cột cho bảng user
        const val TABLE_USER = "user"
        const val COLUMN_ID = "id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"

        // Câu lệnh tạo bảng
        private const val CREATE_TABLE_USER = ("CREATE TABLE $TABLE_USER ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_EMAIL TEXT, "
                + "$COLUMN_PASSWORD TEXT)")
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tạo bảng khi cơ sở dữ liệu được tạo
        db.execSQL(CREATE_TABLE_USER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Nếu nâng cấp cơ sở dữ liệu, hãy xóa bảng cũ và tạo lại bảng mới
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // Hàm thêm người dùng mới vào bảng user
    fun addUser(email: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        val result = db.insert(TABLE_USER, null, values)
        db.close()
        return result != -1L // Trả về true nếu thêm thành công
    }

    // Hàm kiểm tra đăng nhập của người dùng
    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COLUMN_ID),
            "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, password),
            null,
            null,
            null
        )
        val count = cursor.count
        cursor.close()
        db.close()
        return count > 0 // Trả về true nếu có ít nhất một kết quả khớp
    }
}
