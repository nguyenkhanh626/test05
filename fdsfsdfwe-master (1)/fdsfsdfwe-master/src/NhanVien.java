public class NhanVien {

    private String maNhanVien;
    private String hoTen;
    private String phongBan;
    private String sdt;
    private String email;
    private String ngaySinh;
    private String cccd;
    private int thamNien;
    private int diemViPham;
    private int diemThuongDuAn;

    public NhanVien(String maNhanVien, String hoTen, String phongBan, String sdt, String email, String ngaySinh, String cccd, int thamNien) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.phongBan = phongBan;
        this.sdt = sdt;
        this.email = email;
        this.ngaySinh = ngaySinh;
        this.cccd = cccd;
        this.thamNien = thamNien;
        this.diemViPham = 0;
        this.diemThuongDuAn = 0; 
    }

    public String getMaNhanVien() { return maNhanVien; }
    public String getHoTen() { return hoTen; }
    public String getPhongBan() { return phongBan; }
    public String getSdt() { return sdt; }
    public String getEmail() { return email; }
    public String getNgaySinh() { return ngaySinh; }
    public String getCccd() { return cccd; }
    public int getThamNien() { return thamNien; }
    public int getDiemViPham() { return diemViPham; }
    public int getDiemThuongDuAn() { return diemThuongDuAn; }
    
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public void setPhongBan(String phongBan) { this.phongBan = phongBan; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public void setEmail(String email) { this.email = email; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public void setThamNien(int thamNien) { this.thamNien = thamNien; }
    public void addDiemViPham(int points) { this.diemViPham += points; }
    public void setDiemViPham(int diemViPham) { this.diemViPham = diemViPham; }

    
    public void addDiemThuongDuAn(int diem) {
        this.diemThuongDuAn += diem;
    }

    public void setDiemThuongDuAn(int diem) {
        this.diemThuongDuAn = diem;
    }

    @Override
    public String toString() {
        return "NhanVien{" + "maNhanVien='" + maNhanVien + '\'' + ", hoTen='" + hoTen + '\'' + '}';
    }
}
