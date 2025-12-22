import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogEntry {
    private String thoiGian;
    private String nguoiDung;
    private String hanhDong;
    private String chiTiet;

    // Constructor 1: Dùng khi tạo log MỚI (Lấy giờ hiện tại)
    public LogEntry(String nguoiDung, String hanhDong, String chiTiet) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        this.thoiGian = now.format(formatter);
        this.nguoiDung = nguoiDung;
        this.hanhDong = hanhDong;
        this.chiTiet = chiTiet;
    }

    // Constructor 2: Dùng khi load từ DATABASE (Giữ nguyên giờ cũ)
    public LogEntry(String thoiGian, String nguoiDung, String hanhDong, String chiTiet) {
        this.thoiGian = thoiGian;
        this.nguoiDung = nguoiDung;
        this.hanhDong = hanhDong;
        this.chiTiet = chiTiet;
    }

    public String getThoiGian() { return thoiGian; }
    public String getNguoiDung() { return nguoiDung; }
    public String getHanhDong() { return hanhDong; }
    public String getChiTiet() { return chiTiet; }
}