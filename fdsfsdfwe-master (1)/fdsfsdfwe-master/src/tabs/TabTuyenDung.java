package tabs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import MainApp.*;
import dataa.*;

import java.awt.*;
import java.sql.*;

public class TabTuyenDung extends JPanel {

    private QuanLyNhanVienGUI parent;
    private DefaultTableModel modelTin, modelUngVien;
    private JTable tableTin, tableUngVien;
    
    private JTextField txtViTri, txtSoLuong, txtHanNop;
    private JTextField txtTenUV, txtSdtUV, txtEmailUV;
    private JComboBox<String> cmbTrangThaiUV;

    public TabTuyenDung(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createPanelTin(), createPanelUngVien());
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createPanelTin() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách Tin Tuyển dụng"));

        JPanel form = new JPanel(new GridLayout(0, 2));
        form.add(new JLabel("Vị trí:")); txtViTri = new JTextField(); form.add(txtViTri);
        form.add(new JLabel("Số lượng:")); txtSoLuong = new JTextField(); form.add(txtSoLuong);
        form.add(new JLabel("Hạn nộp:")); txtHanNop = new JTextField(); form.add(txtHanNop);
        
        JButton btnThem = new JButton("Đăng tin");
        btnThem.addActionListener(e -> themTin());
        
        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(btnThem, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        modelTin = new DefaultTableModel(new String[]{"ID", "Vị trí", "Số lượng", "Hạn nộp"}, 0);
        tableTin = new JTable(modelTin);
        tableTin.getSelectionModel().addListSelectionListener(e -> loadUngVienTheoTin());
        panel.add(new JScrollPane(tableTin), BorderLayout.CENTER);
        
        loadTinTuyenDung();
        return panel;
    }

    private JPanel createPanelUngVien() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Hồ sơ Ứng viên"));

        JPanel form = new JPanel(new GridLayout(0, 2));
        form.add(new JLabel("Họ tên:")); txtTenUV = new JTextField(); form.add(txtTenUV);
        form.add(new JLabel("SĐT:")); txtSdtUV = new JTextField(); form.add(txtSdtUV);
        form.add(new JLabel("Email:")); txtEmailUV = new JTextField(); form.add(txtEmailUV);
        form.add(new JLabel("Trạng thái:")); 
        cmbTrangThaiUV = new JComboBox<>(new String[]{"Mới", "Phỏng vấn", "Đạt", "Loại"});
        form.add(cmbTrangThaiUV);

        JButton btnThemUV = new JButton("Thêm Ứng viên");
        btnThemUV.addActionListener(e -> themUngVien());
        
        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(btnThemUV, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        modelUngVien = new DefaultTableModel(new String[]{"ID", "Họ tên", "SĐT", "Email", "Trạng thái"}, 0);
        tableUngVien = new JTable(modelUngVien);
        panel.add(new JScrollPane(tableUngVien), BorderLayout.CENTER);

        return panel;
    }

    private void loadTinTuyenDung() {
        modelTin.setRowCount(0);
        try (Connection conn = DatabaseHandler.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tin_tuyen_dung")) {
            while(rs.next()) {
                modelTin.addRow(new Object[]{rs.getInt("id"), rs.getString("vi_tri"), rs.getInt("so_luong"), rs.getString("han_nop")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void themTin() {
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO tin_tuyen_dung(vi_tri, so_luong, han_nop, trang_thai) VALUES(?,?,?,?)")) {
            pstmt.setString(1, txtViTri.getText());
            pstmt.setInt(2, Integer.parseInt(txtSoLuong.getText()));
            pstmt.setString(3, txtHanNop.getText());
            pstmt.setString(4, "Đang tuyển");
            pstmt.executeUpdate();
            loadTinTuyenDung();
            parent.ghiNhatKy("Tuyển dụng", "Đăng tin mới: " + txtViTri.getText());
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi nhập liệu!"); }
    }

    private void loadUngVienTheoTin() {
        int row = tableTin.getSelectedRow();
        if (row == -1) return;
        int tinId = Integer.parseInt(modelTin.getValueAt(row, 0).toString());
        
        modelUngVien.setRowCount(0);
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM ung_vien WHERE tin_tuyen_dung_id = ?")) {
            pstmt.setInt(1, tinId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                modelUngVien.addRow(new Object[]{rs.getInt("id"), rs.getString("ho_ten"), rs.getString("sdt"), rs.getString("email"), rs.getString("trang_thai")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void themUngVien() {
        int row = tableTin.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn tin tuyển dụng trước!"); return; }
        int tinId = Integer.parseInt(modelTin.getValueAt(row, 0).toString());

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO ung_vien(ho_ten, sdt, email, tin_tuyen_dung_id, trang_thai) VALUES(?,?,?,?,?)")) {
            pstmt.setString(1, txtTenUV.getText());
            pstmt.setString(2, txtSdtUV.getText());
            pstmt.setString(3, txtEmailUV.getText());
            pstmt.setInt(4, tinId);
            pstmt.setString(5, cmbTrangThaiUV.getSelectedItem().toString());
            pstmt.executeUpdate();
            loadUngVienTheoTin();
            parent.ghiNhatKy("Tuyển dụng", "Nhận hồ sơ: " + txtTenUV.getText());
        } catch (Exception e) { e.printStackTrace(); }
    }
}