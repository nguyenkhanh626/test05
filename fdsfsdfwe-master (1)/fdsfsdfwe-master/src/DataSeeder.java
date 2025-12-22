import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataSeeder {

    // Kho dữ liệu mẫu
    private static final String[] HO = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng", "Bùi", "Đỗ"};
    private static final String[] TEN_DEM = {"Văn", "Thị", "Đức", "Thành", "Ngọc", "Minh", "Quốc", "Gia", "Bảo", "Hữu", "Thanh", "Mạnh"};
    private static final String[] TEN = {"Hùng", "Lan", "Tuấn", "Hương", "Dũng", "Hoa", "Nam", "Mai", "Cường", "Trang", "Huy", "Thảo", "Long", "Vân"};

    /**
     * Hàm này nhận Connection từ GUI để thêm dữ liệu mà không gây xung đột
     */
    public static void themNhanVienMau(Connection conn) {
        if (conn == null) return;

        try {
            // 1. Lấy danh sách tên các phòng ban hiện có
            List<String> listPhongBan = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT ten_pb FROM phong_ban")) {
                while (rs.next()) {
                    listPhongBan.add(rs.getString("ten_pb"));
                }
            }

            if (listPhongBan.isEmpty()) return; // Không có phòng ban thì không thêm NV

            // 2. Chuẩn bị câu lệnh Insert
            String sql = "INSERT INTO nhan_vien (ma_nv, ho_ten, phong_ban, sdt, email, ngay_sinh, cccd, tham_nien, diem_vi_pham, diem_thuong_da) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            // Tắt auto-commit để chạy Batch cho nhanh và an toàn
            boolean autoCommitCu = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Random rand = new Random();
                int startID = 0; 

                // Mỗi phòng ban thêm 10 người
                for (String tenPB : listPhongBan) {
                    for (int i = 0; i < 10; i++) {
                        startID++;
                        String maNV=startID<10 ? "0"+startID : String.valueOf(startID);
                        maNV="NV0" + maNV;
                        String ho = HO[rand.nextInt(HO.length)];
                        String dem = TEN_DEM[rand.nextInt(TEN_DEM.length)];
                        String ten = TEN[rand.nextInt(TEN.length)];
                        String hoTen = ho + " " + dem + " " + ten;
                        String email = removeAccent(ten.toLowerCase()) + startID + "@company.com";
                        String sdt = "09" + (10000000 + rand.nextInt(89999999));
                        String cccd = "0" + (10000000000L + rand.nextLong(89999999999L));
                        int year = 1985 + rand.nextInt(20);
                        String ngaySinh = String.format("%02d/%02d/%d", 1 + rand.nextInt(28), 1 + rand.nextInt(12), year);

                        pstmt.setString(1, maNV);
                        pstmt.setString(2, hoTen);
                        pstmt.setString(3, tenPB);
                        pstmt.setString(4, sdt);
                        pstmt.setString(5, email);
                        pstmt.setString(6, ngaySinh);
                        pstmt.setString(7, cccd);
                        pstmt.setInt(8, rand.nextInt(10));
                        pstmt.setInt(9, 0);
                        pstmt.setInt(10, 0);
                        pstmt.addBatch();
                    }
                }
                pstmt.executeBatch();
                conn.commit(); // Xác nhận lưu vào DB
                System.out.println("✅ Đã sinh dữ liệu mẫu thành công!");
            } catch (Exception e) {
                conn.rollback(); // Nếu lỗi thì hoàn tác
                e.printStackTrace();
            } finally {
                // Trả lại trạng thái commit cũ cho Connection của GUI
                conn.setAutoCommit(autoCommitCu);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String removeAccent(String s) {
        String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ','d').replace('Đ','d');
    }
}