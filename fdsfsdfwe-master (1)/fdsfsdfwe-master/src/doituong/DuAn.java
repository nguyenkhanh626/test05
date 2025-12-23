package doituong;
import java.util.ArrayList;
import java.util.List;


public class DuAn {

    private String maDuAn;
    private String tenDuAn;
    private int doPhucTap; 

    private List<NhanVien> danhSachThanhVien;

    public DuAn(String maDuAn, String tenDuAn, int doPhucTap) {
        this.maDuAn = maDuAn;
        this.tenDuAn = tenDuAn;
        this.doPhucTap = doPhucTap;
        this.danhSachThanhVien = new ArrayList<>(); 
    }

    
    public String getMaDuAn() {
        return maDuAn;
    }

    public String getTenDuAn() {
        return tenDuAn;
    }

    public int getDoPhucTap() {
        return doPhucTap;
    }

    public List<NhanVien> getDanhSachThanhVien() {
        return danhSachThanhVien;
    }

    public void setTenDuAn(String tenDuAn) {
        this.tenDuAn = tenDuAn;
    }

    public void setDoPhucTap(int doPhucTap) {
        this.doPhucTap = doPhucTap;
    }

    public void addThanhVien(NhanVien nv) {
        this.danhSachThanhVien.add(nv);
    }
    
    public boolean hasThanhVien(NhanVien nv) {
        return this.danhSachThanhVien.contains(nv);
    }

    @Override
    public String toString() {
        return this.tenDuAn;
    }
}
