import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class QuanLyTaiKhoan {

    public QuanLyTaiKhoan() {
        if (!kiemTraUserTonTai("admin")) {
            themTaiKhoan("admin", "admin");
            System.out.println("Đã tạo tài khoản admin/admin mặc định.");
        }
    }

    public boolean kiemTraDangNhap(String username, String passwordInput) {
        String sql = "SELECT password_hash, salt FROM tai_khoan WHERE username = ?";
        
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                
                String newHash = hashPassword(passwordInput, salt);
                return newHash.equals(storedHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean themTaiKhoan(String username, String password) {
        if (kiemTraUserTonTai(username)) return false;

        String salt = taoMuoiNgauNhien();
        String hashedPassword = hashPassword(password, salt);
        String sql = "INSERT INTO tai_khoan(username, password_hash, salt) VALUES(?,?,?)";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean doiMatKhau(String username, String matKhauCu, String matKhauMoi) {
        if (kiemTraDangNhap(username, matKhauCu)) {
            String saltMoi = taoMuoiNgauNhien();
            String hashMoi = hashPassword(matKhauMoi, saltMoi);
            
            String sql = "UPDATE tai_khoan SET password_hash = ?, salt = ? WHERE username = ?";
            try (Connection conn = DatabaseHandler.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, hashMoi);
                pstmt.setString(2, saltMoi);
                pstmt.setString(3, username);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean kiemTraUserTonTai(String username) {
        String sql = "SELECT 1 FROM tai_khoan WHERE username = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String taoMuoiNgauNhien() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}