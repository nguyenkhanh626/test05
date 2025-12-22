import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {
    
    // Đường dẫn file database SQLite
    private static final String URL = "jdbc:sqlite:quanlynhansu.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void createNewDatabase() {
        
        String sqlPB = "CREATE TABLE IF NOT EXISTS phong_ban ("
                + " ma_pb TEXT PRIMARY KEY, ten_pb TEXT NOT NULL)";

        // Bảng nhân viên
        String sqlNV = "CREATE TABLE IF NOT EXISTS nhan_vien ("
                + " ma_nv TEXT PRIMARY KEY, ho_ten TEXT, phong_ban TEXT,"
                + " sdt TEXT, email TEXT, ngay_sinh TEXT, cccd TEXT,"
                + " tham_nien INTEGER, diem_vi_pham INTEGER DEFAULT 0,"
                + " diem_thuong_da INTEGER DEFAULT 0)";

        // Bảng tài khoản
        String sqlTK = "CREATE TABLE IF NOT EXISTS tai_khoan ("
                + " username TEXT PRIMARY KEY, password_hash TEXT, salt TEXT)";

        // Bảng nhật ký
        String sqlLog = "CREATE TABLE IF NOT EXISTS nhat_ky ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT, thoi_gian TEXT,"
                + " nguoi_dung TEXT, hanh_dong TEXT, chi_tiet TEXT)";

        // Bảng dự án
        String sqlDA = "CREATE TABLE IF NOT EXISTS du_an ("
                + " ma_da TEXT PRIMARY KEY, ten_da TEXT, do_phuc_tap INTEGER)";

        // [MỚI] Bảng phân công (Lưu quan hệ nhiều-nhiều giữa Dự án và Nhân viên)
        String sqlPhanCong = "CREATE TABLE IF NOT EXISTS phan_cong ("
                + " ma_da TEXT, ma_nv TEXT, "
                + " PRIMARY KEY(ma_da, ma_nv))";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sqlPB);
            stmt.execute(sqlNV);
            stmt.execute(sqlTK);
            stmt.execute(sqlLog);
            stmt.execute(sqlDA);
            stmt.execute(sqlPhanCong); // Tạo bảng phân công
            
            System.out.println("✅ Kết nối CSDL và kiểm tra bảng thành công!");
            
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khởi tạo DB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}