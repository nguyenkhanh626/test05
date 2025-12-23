package doituong;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import dataa.DatabaseHandler;

public class QuanLyTaiKhoan {

    public QuanLyTaiKhoan() {
        if (!kiemTraUserTonTai("admin")) {
            themTaiKhoan("admin", "admin", "admin");
            System.out.println("Đã tạo tài khoản admin/admin (Role: admin).");
        }
    }

    // Trả về Role nếu đăng nhập thành công, null nếu thất bại
    public String dangNhap(String username, String passwordInput) {
        String sql = "SELECT password_hash, salt, role FROM tai_khoan WHERE username = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                String role = rs.getString("role");
                
                String newHash = hashPassword(passwordInput, salt);
                if (newHash.equals(storedHash)) {
                    return role; // Trả về vai trò (admin, hr, accountant...)
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    
    
    public boolean kiemTraDangNhap(String username, String password) {
        return dangNhap(username, password) != null;
    }

    public boolean themTaiKhoan(String username, String password, String role) {
        if (kiemTraUserTonTai(username)) return false;

        String salt = taoMuoiNgauNhien();
        String hashedPassword = hashPassword(password, salt);
        String sql = "INSERT INTO tai_khoan(username, password_hash, salt, role) VALUES(?,?,?,?)";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, salt);
            pstmt.setString(4, role);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    /* 
    public boolean themTaiKhoan(String username, String password) {
        return themTaiKhoan(username, password, "user");
    }
    */
    public boolean doiMatKhau(String username, String matKhauCu, String matKhauMoi) {
        if (kiemTraDangNhap(username, matKhauCu)) {
            String saltMoi = taoMuoiNgauNhien();
            String hashMoi = hashPassword(matKhauMoi, saltMoi);
            
            String sql = "UPDATE tai_khoan SET password_hash = ?, salt = ? WHERE username = ?";
            try (Connection conn = DatabaseHandler.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, hashMoi); pstmt.setString(2, saltMoi); pstmt.setString(3, username);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    private boolean kiemTraUserTonTai(String username) {
        String sql = "SELECT 1 FROM tai_khoan WHERE username = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            return Base64.getEncoder().encodeToString(md.digest(password.getBytes()));
        } catch (NoSuchAlgorithmException e) { return null; }
    }

    private String taoMuoiNgauNhien() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}