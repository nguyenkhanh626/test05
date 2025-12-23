package dataa;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.crypto.Data;

public class DatabaseHandler {
    
    // Đường dẫn file database SQLite
    private static final String URL = "jdbc:sqlite:quanlynhansu.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void createNewDatabase() {
        
        //CÁC BẢNG CƠ BẢN
        String sqlPB = "CREATE TABLE IF NOT EXISTS phong_ban ("
                     + " ma_pb TEXT PRIMARY KEY, "
                     + " ten_pb TEXT NOT NULL)";

        String sqlNV = "CREATE TABLE IF NOT EXISTS nhan_vien ("
                     + " ma_nv TEXT PRIMARY KEY, "
                     + " ho_ten TEXT, "
                     + " phong_ban TEXT, "
                     + " sdt TEXT, "
                     + " email TEXT, "
                     + " ngay_sinh TEXT, "
                     + " cccd TEXT, "
                     + " tham_nien INTEGER, "
                     + " diem_vi_pham INTEGER DEFAULT 0, "
                     + " diem_thuong_da INTEGER DEFAULT 0)";
        
        String sqlTK = "CREATE TABLE IF NOT EXISTS tai_khoan ("
                     + " username TEXT PRIMARY KEY, "
                     + " password_hash TEXT, "
                     + " salt TEXT, "
                     + " role TEXT DEFAULT 'user')";

        String sqlLog = "CREATE TABLE IF NOT EXISTS nhat_ky ("
                      + " id INTEGER PRIMARY KEY AUTOINCREMENT, "
                      + " thoi_gian TEXT, "
                      + " nguoi_dung TEXT, "
                      + " hanh_dong TEXT, "
                      + " chi_tiet TEXT)";

        String sqlDA = "CREATE TABLE IF NOT EXISTS du_an ("
                     + " ma_da TEXT PRIMARY KEY, "
                     + " ten_da TEXT, "
                     + " do_phuc_tap INTEGER)";

        String sqlPhanCong = "CREATE TABLE IF NOT EXISTS phan_cong ("
                           + " ma_da TEXT, "
                           + " ma_nv TEXT, "
                           + " PRIMARY KEY(ma_da, ma_nv))";

        //CÁC BẢNG CHẤM CÔNG
        String sqlCa = "CREATE TABLE IF NOT EXISTS ca_lam_viec ("
                     + " id INTEGER PRIMARY KEY AUTOINCREMENT, "
                     + " ten_ca TEXT, "
                     + " gio_bat_dau TEXT, "
                     + " gio_ket_thuc TEXT)";

        String sqlChamCong = "CREATE TABLE IF NOT EXISTS cham_cong ("
                           + " id INTEGER PRIMARY KEY AUTOINCREMENT, "
                           + " ma_nv TEXT, "
                           + " ngay_lam_viec TEXT, "
                           + " ma_ca INTEGER, "
                           + " gio_vao TEXT, "
                           + " gio_ra TEXT, "
                           + " ghi_chu TEXT)";

        String sqlNghiPhep = "CREATE TABLE IF NOT EXISTS don_nghi_phep ("
                           + " id INTEGER PRIMARY KEY AUTOINCREMENT, "
                           + " ma_nv TEXT, "
                           + " tu_ngay TEXT, "
                           + " den_ngay TEXT, "
                           + " ly_do TEXT, "
                           + " trang_thai TEXT, "
                           + " ngay_tao TEXT)";

        //CÁC BẢNG còn lại
        String sqlTinTuyenDung = "CREATE TABLE IF NOT EXISTS tin_tuyen_dung ("
                               + " id INTEGER PRIMARY KEY AUTOINCREMENT, "
                               + " vi_tri TEXT, "
                               + " so_luong INTEGER, "
                               + " han_nop TEXT, "
                               + " trang_thai TEXT)";

        String sqlUngVien = "CREATE TABLE IF NOT EXISTS ung_vien ("
                          + " id INTEGER PRIMARY KEY AUTOINCREMENT, "
                          + " ho_ten TEXT, "
                          + " sdt TEXT, "
                          + " email TEXT, "
                          + " tin_tuyen_dung_id INTEGER, "
                          + " trang_thai TEXT)";

        String sqlKhoaDaoTao = "CREATE TABLE IF NOT EXISTS khoa_dao_tao ("
                             + " ma_khoa TEXT PRIMARY KEY, "
                             + " ten_khoa TEXT, "
                             + " ngay_bat_dau TEXT, "
                             + " ngay_ket_thuc TEXT, "
                             + " mo_ta TEXT)";

        String sqlHocVien = "CREATE TABLE IF NOT EXISTS hoc_vien ("
                          + " ma_khoa TEXT, "
                          + " ma_nv TEXT, "
                          + " ket_qua TEXT, "
                          + " PRIMARY KEY(ma_khoa, ma_nv))";

        String sqlTaiSan = "CREATE TABLE IF NOT EXISTS tai_san ("
                         + " ma_ts TEXT PRIMARY KEY, "
                         + " ten_ts TEXT, "
                         + " loai_ts TEXT, "
                         + " tinh_trang TEXT, "
                         + " ma_nv_su_dung TEXT)";

        //BẢNG LỊCH LÀM VIỆC
        // Lưu trữ kế hoạch phân ca cho từng ngày
        String sqlLichLamViec = "CREATE TABLE IF NOT EXISTS lich_lam_viec ("
                              + " id INTEGER PRIMARY KEY AUTOINCREMENT, "
                              + " ma_nv TEXT, "
                              + " ngay TEXT, " // dd/MM/yyyy
                              + " ma_ca INTEGER, "
                              + " loai_phan_cong TEXT DEFAULT 'ChinhThuc', " // ChinhThuc, TangCa
                              + " ghi_chu TEXT, "
                              + " CONSTRAINT uq_lich UNIQUE(ma_nv, ngay, ma_ca))";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sqlPB);
            stmt.execute(sqlNV);
            stmt.execute(sqlTK);
            stmt.execute(sqlLog);
            stmt.execute(sqlDA);
            stmt.execute(sqlPhanCong);
            stmt.execute(sqlCa);
            stmt.execute(sqlChamCong);
            stmt.execute(sqlNghiPhep);
            stmt.execute(sqlTinTuyenDung);
            stmt.execute(sqlUngVien);
            stmt.execute(sqlKhoaDaoTao);
            stmt.execute(sqlHocVien);
            stmt.execute(sqlTaiSan);
            
            
            stmt.execute(sqlLichLamViec);

            
            try {
                stmt.execute("ALTER TABLE tai_khoan ADD COLUMN role TEXT DEFAULT 'user'");
            } catch (SQLException e) { /* Cột đã tồn tại */ }
            
            stmt.executeUpdate("UPDATE tai_khoan SET role = 'admin' WHERE username = 'admin'");

            // Dữ liệu mẫu Ca làm việc
            ResultSet rsCa = stmt.executeQuery("SELECT COUNT(*) FROM ca_lam_viec");
            rsCa.next();
            if (rsCa.getInt(1) == 0) {
                stmt.execute("INSERT INTO ca_lam_viec (ten_ca, gio_bat_dau, gio_ket_thuc) VALUES ('Ca Sáng', '08:00', '12:00')");
                stmt.execute("INSERT INTO ca_lam_viec (ten_ca, gio_bat_dau, gio_ket_thuc) VALUES ('Ca Chiều', '13:00', '17:00')");
            }

            System.out.println("Database đã được khởi tạo/cập nhật đầy đủ (bao gồm bảng Lịch Làm Việc).");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        

    }
}