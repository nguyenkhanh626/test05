package tabs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import MainApp.*;
import dataa.*;

import java.awt.*;
import java.sql.*;

public class TabDaoTao extends JPanel {
    private QuanLyNhanVienGUI parent;
    private DefaultTableModel modelKhoa, modelHocVien;
    private JTable tableKhoa, tableHocVien;
    private JTextField txtMaKhoa, txtTenKhoa, txtNgayBD, txtNgayKT;
    private JTextField txtMaNVHoc;

    public TabDaoTao(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createPanelKhoaHoc(), createPanelHocVien());
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createPanelKhoaHoc() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Danh mục Khóa Đào tạo"));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Mã Khóa:")); txtMaKhoa = new JTextField(8); form.add(txtMaKhoa);
        form.add(new JLabel("Tên Khóa:")); txtTenKhoa = new JTextField(15); form.add(txtTenKhoa);
        form.add(new JLabel("Ngày BĐ:")); txtNgayBD = new JTextField(8); form.add(txtNgayBD);
        form.add(new JLabel("Ngày KT:")); txtNgayKT = new JTextField(8); form.add(txtNgayKT);
        
        JButton btnThem = new JButton("Tạo Khóa học");
        btnThem.addActionListener(e -> themKhoaHoc());
        form.add(btnThem);

        panel.add(form, BorderLayout.NORTH);

        modelKhoa = new DefaultTableModel(new String[]{"Mã Khóa", "Tên Khóa", "Bắt đầu", "Kết thúc"}, 0);
        tableKhoa = new JTable(modelKhoa);
        tableKhoa.getSelectionModel().addListSelectionListener(e -> loadHocVien());
        panel.add(new JScrollPane(tableKhoa), BorderLayout.CENTER);
        
        loadKhoaHoc();
        return panel;
    }

    private JPanel createPanelHocVien() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách Học viên tham gia"));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Nhập Mã NV để thêm vào khóa:"));
        txtMaNVHoc = new JTextField(10);
        form.add(txtMaNVHoc);
        JButton btnAdd = new JButton("Thêm Học viên");
        btnAdd.addActionListener(e -> themHocVien());
        form.add(btnAdd);

        panel.add(form, BorderLayout.NORTH);

        modelHocVien = new DefaultTableModel(new String[]{"Mã NV", "Họ Tên", "Kết quả"}, 0);
        tableHocVien = new JTable(modelHocVien);
        panel.add(new JScrollPane(tableHocVien), BorderLayout.CENTER);

        return panel;
    }

    private void loadKhoaHoc() {
        modelKhoa.setRowCount(0);
        try (Connection conn = DatabaseHandler.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM khoa_dao_tao")) {
            while(rs.next()) {
                modelKhoa.addRow(new Object[]{rs.getString("ma_khoa"), rs.getString("ten_khoa"), rs.getString("ngay_bat_dau"), rs.getString("ngay_ket_thuc")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void themKhoaHoc() {
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO khoa_dao_tao(ma_khoa, ten_khoa, ngay_bat_dau, ngay_ket_thuc) VALUES(?,?,?,?)")) {
            pstmt.setString(1, txtMaKhoa.getText());
            pstmt.setString(2, txtTenKhoa.getText());
            pstmt.setString(3, txtNgayBD.getText());
            pstmt.setString(4, txtNgayKT.getText());
            pstmt.executeUpdate();
            loadKhoaHoc();
            parent.ghiNhatKy("Đào tạo", "Tạo khóa mới: " + txtTenKhoa.getText());
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi tạo khóa học (Trùng mã?)"); }
    }

    private void loadHocVien() {
        int row = tableKhoa.getSelectedRow();
        if (row == -1) return;
        String maKhoa = modelKhoa.getValueAt(row, 0).toString();
        
        modelHocVien.setRowCount(0);
        String sql = "SELECT hv.ma_nv, nv.ho_ten, hv.ket_qua FROM hoc_vien hv JOIN nhan_vien nv ON hv.ma_nv = nv.ma_nv WHERE hv.ma_khoa = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKhoa);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                modelHocVien.addRow(new Object[]{rs.getString("ma_nv"), rs.getString("ho_ten"), rs.getString("ket_qua")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void themHocVien() {
        int row = tableKhoa.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn khóa học trước!"); return; }
        String maKhoa = modelKhoa.getValueAt(row, 0).toString();
        String maNV = txtMaNVHoc.getText().trim();

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO hoc_vien(ma_khoa, ma_nv, ket_qua) VALUES(?,?,?)")) {
            pstmt.setString(1, maKhoa);
            pstmt.setString(2, maNV);
            pstmt.setString(3, "Đang học");
            pstmt.executeUpdate();
            loadHocVien();
            parent.ghiNhatKy("Đào tạo", "Thêm NV " + maNV + " vào khóa " + maKhoa);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi! NV có thể đã trong khóa hoặc không tồn tại."); }
    }
}