import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TabNhanVien extends JPanel {

    private QuanLyNhanVienGUI parent;
    private List<NhanVien> danhSachNV;
    private List<PhongBan> danhSachPB;

    private JComboBox<PhongBan> cmbPhongBanNV;
    private DefaultTableModel modelNV;
    private JTable tableNV;
    private JTextField txtMaNV, txtTenNV, txtSdt, txtEmail, txtNgaySinh, txtCccd, txtThamNien;
    private JComboBox<String> cmbTieuChiTimKiem;
    private JTextField txtTuKhoaTimKiem;

    public TabNhanVien(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        this.danhSachNV = parent.danhSachNV; 
        this.danhSachPB = parent.danhSachPB; 

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm & Lọc"));
        
        searchPanel.add(new JLabel("Tiêu chí:"));
        String[] tieuChi = {"Mã Nhân viên", "Tên Nhân viên"};
        cmbTieuChiTimKiem = new JComboBox<>(tieuChi);
        searchPanel.add(cmbTieuChiTimKiem);
        
        searchPanel.add(new JLabel("    Từ khóa:"));
        txtTuKhoaTimKiem = new JTextField(20);
        searchPanel.add(txtTuKhoaTimKiem);
        
        txtTuKhoaTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { xuLyTimKiem(); }
            @Override public void removeUpdate(DocumentEvent e) { xuLyTimKiem(); }
            @Override public void changedUpdate(DocumentEvent e) { xuLyTimKiem(); }
        });
        
        cmbTieuChiTimKiem.addActionListener(e -> xuLyTimKiem());
        topPanel.add(searchPanel, BorderLayout.NORTH); 

        JPanel formPanel = new JPanel(new GridLayout(0, 4, 10, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết")); 
        
        formPanel.add(new JLabel("Mã NV:")); txtMaNV = new JTextField(); formPanel.add(txtMaNV);
        formPanel.add(new JLabel("Tên NV:")); txtTenNV = new JTextField(); formPanel.add(txtTenNV);
        formPanel.add(new JLabel("Phòng ban:")); cmbPhongBanNV = new JComboBox<>(); formPanel.add(cmbPhongBanNV);
        formPanel.add(new JLabel("SĐT:")); txtSdt = new JTextField(); formPanel.add(txtSdt);
        formPanel.add(new JLabel("Email:")); txtEmail = new JTextField(); formPanel.add(txtEmail);
        formPanel.add(new JLabel("Ngày sinh:")); txtNgaySinh = new JTextField(); formPanel.add(txtNgaySinh);
        formPanel.add(new JLabel("CCCD:")); txtCccd = new JTextField(); formPanel.add(txtCccd);
        formPanel.add(new JLabel("Thâm niên:")); txtThamNien = new JTextField(); formPanel.add(txtThamNien);
        
        topPanel.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnThemNV = new JButton("Thêm");
        JButton btnSuaNV = new JButton("Sửa");
        JButton btnXoaNV = new JButton("Xóa");
        JButton btnLamMoiNV = new JButton("Làm mới");

        btnThemNV.addActionListener(e -> themNhanVien());
        btnSuaNV.addActionListener(e -> suaNhanVien());
        btnXoaNV.addActionListener(e -> xoaNhanVien());
        btnLamMoiNV.addActionListener(e -> lamMoiFormNV());

        buttonPanel.add(btnThemNV); buttonPanel.add(btnSuaNV);
        buttonPanel.add(btnXoaNV); buttonPanel.add(btnLamMoiNV);
        
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"Mã NV", "Họ Tên", "Phòng ban", "SĐT", "Email", "Ngày sinh", "CCCD", "Thâm niên"};
        modelNV = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableNV = new JTable(modelNV);

        tableNV.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { hienThiThongTinLenFormNV(); }
        });
        
        add(new JScrollPane(tableNV), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void xuLyTimKiem() {
        String tuKhoa = txtTuKhoaTimKiem.getText().trim().toLowerCase();
        String tieuChi = (String) cmbTieuChiTimKiem.getSelectedItem();
        
        modelNV.setRowCount(0);
        for (NhanVien nv : danhSachNV) {
            boolean thoaMan = false;
            if (tuKhoa.isEmpty()) {
                thoaMan = true; 
            } else {
                if ("Mã Nhân viên".equals(tieuChi)) {
                    if (nv.getMaNhanVien().toLowerCase().contains(tuKhoa)) thoaMan = true;
                } else if ("Tên Nhân viên".equals(tieuChi)) {
                    if (nv.getHoTen().toLowerCase().contains(tuKhoa)) thoaMan = true;
                }
            }
            if (thoaMan) {
                modelNV.addRow(new Object[]{
                    nv.getMaNhanVien(), nv.getHoTen(), nv.getPhongBan(),
                    nv.getSdt(), nv.getEmail(), nv.getNgaySinh(),
                    nv.getCccd(), nv.getThamNien()
                });
            }
        }
    }

    private void hienThiThongTinLenFormNV() {
        int r = tableNV.getSelectedRow();
        if (r == -1) return;
        txtMaNV.setText(modelNV.getValueAt(r, 0).toString());
        txtTenNV.setText(modelNV.getValueAt(r, 1).toString());
        String tenPhongBan = modelNV.getValueAt(r, 2).toString();
        txtSdt.setText(modelNV.getValueAt(r, 3).toString());
        txtEmail.setText(modelNV.getValueAt(r, 4).toString());
        txtNgaySinh.setText(modelNV.getValueAt(r, 5).toString());
        txtCccd.setText(modelNV.getValueAt(r, 6).toString());
        txtThamNien.setText(String.valueOf(modelNV.getValueAt(r, 7)));
        for (int i = 0; i < cmbPhongBanNV.getItemCount(); i++) {
            if (cmbPhongBanNV.getItemAt(i).getTenPhongBan().equals(tenPhongBan)) {
                cmbPhongBanNV.setSelectedIndex(i);
                break;
            }
        }
        txtMaNV.setEditable(false);
    }

    private void themNhanVien() {
        String maNV = txtMaNV.getText();
        String tenNV = txtTenNV.getText();
        PhongBan pb = (PhongBan) cmbPhongBanNV.getSelectedItem();
        String sdt = txtSdt.getText();
        String email = txtEmail.getText();
        String ngaySinh = txtNgaySinh.getText();
        String cccd = txtCccd.getText();
        
        int thamNien = 0;
        try { thamNien = Integer.parseInt(txtThamNien.getText()); } catch(Exception ex) {}

        if (maNV.isEmpty() || tenNV.isEmpty() || pb == null) return;
        if (danhSachNV.stream().anyMatch(nv -> nv.getMaNhanVien().equals(maNV))) {
            JOptionPane.showMessageDialog(this, "Mã nhân viên đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
        }

        //SQL INSERT
        String sql = "INSERT INTO nhan_vien(ma_nv, ho_ten, phong_ban, sdt, email, ngay_sinh, cccd, tham_nien) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maNV);
            pstmt.setString(2, tenNV);
            pstmt.setString(3, pb.getTenPhongBan());
            pstmt.setString(4, sdt);
            pstmt.setString(5, email);
            pstmt.setString(6, ngaySinh);
            pstmt.setString(7, cccd);
            pstmt.setInt(8, thamNien);
            pstmt.executeUpdate();
            
            NhanVien nv = new NhanVien(maNV, tenNV, pb.getTenPhongBan(), sdt, email, ngaySinh, cccd, thamNien);
            danhSachNV.add(nv);
            xuLyTimKiem();
            lamMoiFormNV();
            parent.ghiNhatKy("Thêm nhân viên", "Đã thêm NV mới: " + maNV);
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            parent.refreshAllTabs();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi Database: " + e.getMessage());
        }
    }

    private void suaNhanVien() {
        int r = tableNV.getSelectedRow();
        if (r == -1) return;
        String maNV = txtMaNV.getText(); 
        String tenMoi = txtTenNV.getText();
        PhongBan pbMoi = (PhongBan) cmbPhongBanNV.getSelectedItem();
        String sdtMoi = txtSdt.getText();
        String emailMoi = txtEmail.getText();
        String ngaySinhMoi = txtNgaySinh.getText();
        String cccdMoi = txtCccd.getText();
        int thamNienMoi = 0;
        try { thamNienMoi = Integer.parseInt(txtThamNien.getText()); } catch(Exception ex) {}

        //SQL UPDATE
        String sql = "UPDATE nhan_vien SET ho_ten=?, phong_ban=?, sdt=?, email=?, ngay_sinh=?, cccd=?, tham_nien=? WHERE ma_nv=?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenMoi);
            pstmt.setString(2, pbMoi.getTenPhongBan());
            pstmt.setString(3, sdtMoi);
            pstmt.setString(4, emailMoi);
            pstmt.setString(5, ngaySinhMoi);
            pstmt.setString(6, cccdMoi);
            pstmt.setInt(7, thamNienMoi);
            pstmt.setString(8, maNV);
            pstmt.executeUpdate();
            
            for(NhanVien nv : danhSachNV) {
                if(nv.getMaNhanVien().equals(maNV)) {
                    nv.setHoTen(tenMoi);
                    nv.setPhongBan(pbMoi.getTenPhongBan());
                    nv.setSdt(sdtMoi);
                    nv.setEmail(emailMoi);
                    nv.setNgaySinh(ngaySinhMoi);
                    nv.setCccd(cccdMoi);
                    nv.setThamNien(thamNienMoi);
                    break;
                }
            }
            xuLyTimKiem();
            lamMoiFormNV();
            parent.ghiNhatKy("Sửa nhân viên", "Cập nhật thông tin NV: " + maNV);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            parent.refreshAllTabs();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void xoaNhanVien() {
        int r = tableNV.getSelectedRow();
        if (r == -1) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String maNV = modelNV.getValueAt(r, 0).toString();
            
            //SQL DELETE
            String sql = "DELETE FROM nhan_vien WHERE ma_nv=?";
            try (Connection conn = DatabaseHandler.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maNV);
                pstmt.executeUpdate();
                
                danhSachNV.removeIf(nv -> nv.getMaNhanVien().equals(maNV));
                xuLyTimKiem();
                lamMoiFormNV();
                parent.ghiNhatKy("Xóa nhân viên", "Đã xóa NV: " + maNV);
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                parent.refreshAllTabs();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void lamMoiFormNV() {
        txtMaNV.setText("");
        txtTenNV.setText("");
        if(cmbPhongBanNV.getItemCount() > 0) cmbPhongBanNV.setSelectedIndex(0);
        txtSdt.setText("");
        txtEmail.setText("");
        txtNgaySinh.setText("");
        txtCccd.setText("");
        txtThamNien.setText("");
        txtMaNV.setEditable(true);
        tableNV.clearSelection();
    }
    
    public void refreshTableNV() {
        txtTuKhoaTimKiem.setText(""); 
        xuLyTimKiem();
    }
    
    public void updatePhongBanComboBox() {
        cmbPhongBanNV.removeAllItems();
        for (PhongBan pb : danhSachPB) cmbPhongBanNV.addItem(pb);
    }
}