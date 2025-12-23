package tabs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import MainApp.*;
import dataa.*;

import java.awt.*;
import java.sql.*;

public class TabTaiSan extends JPanel {
    private QuanLyNhanVienGUI parent;
    private DefaultTableModel modelTaiSan;
    private JTable tableTaiSan;
    private JTextField txtMaTS, txtTenTS;
    private JComboBox<String> cmbLoaiTS;
    private JTextField txtMaNVSudung;

    public TabTaiSan(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Thêm Tài sản mới"));
        
        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.add(new JLabel("Mã Tài sản:")); txtMaTS = new JTextField(); form.add(txtMaTS);
        form.add(new JLabel("Tên Tài sản:")); txtTenTS = new JTextField(); form.add(txtTenTS);
        form.add(new JLabel("Loại:")); 
        cmbLoaiTS = new JComboBox<>(new String[]{"Máy tính", "Xe cộ", "Bàn ghế", "Khác"});
        form.add(cmbLoaiTS);
        JButton btnThem = new JButton("Thêm vào Kho");
        btnThem.addActionListener(e -> themTaiSan());
        
        leftPanel.add(form, BorderLayout.CENTER);
        leftPanel.add(btnThem, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(250, 0));
        add(leftPanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Danh sách & Cấp phát"));
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(new JLabel("Mã NV nhận bàn giao:"));
        txtMaNVSudung = new JTextField(10); actionPanel.add(txtMaNVSudung);
        JButton btnCapPhat = new JButton("Cấp phát");
        btnCapPhat.setBackground(new Color(0, 102, 204)); btnCapPhat.setForeground(Color.WHITE);
        btnCapPhat.addActionListener(e -> capPhatTaiSan());
        
        JButton btnThuHoi = new JButton("Thu hồi");
        btnThuHoi.setBackground(new Color(204, 0, 0)); btnThuHoi.setForeground(Color.WHITE);
        btnThuHoi.addActionListener(e -> thuHoiTaiSan());
        
        actionPanel.add(btnCapPhat);
        actionPanel.add(btnThuHoi);
        centerPanel.add(actionPanel, BorderLayout.NORTH);

        modelTaiSan = new DefaultTableModel(new String[]{"Mã TS", "Tên TS", "Loại", "Người đang dùng"}, 0);
        tableTaiSan = new JTable(modelTaiSan);
        centerPanel.add(new JScrollPane(tableTaiSan), BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        loadTaiSan();
    }

    private void loadTaiSan() {
        modelTaiSan.setRowCount(0);
        try (Connection conn = DatabaseHandler.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tai_san")) {
            while(rs.next()) {
                String nguoiDung = rs.getString("ma_nv_su_dung");
                modelTaiSan.addRow(new Object[]{rs.getString("ma_ts"), rs.getString("ten_ts"), rs.getString("loai_ts"), (nguoiDung == null ? "-- Kho --" : nguoiDung)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void themTaiSan() {
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO tai_san(ma_ts, ten_ts, loai_ts, tinh_trang) VALUES(?,?,?,?)")) {
            pstmt.setString(1, txtMaTS.getText());
            pstmt.setString(2, txtTenTS.getText());
            pstmt.setString(3, cmbLoaiTS.getSelectedItem().toString());
            pstmt.setString(4, "Mới");
            pstmt.executeUpdate();
            loadTaiSan();
            parent.ghiNhatKy("Tài sản", "Nhập kho: " + txtTenTS.getText());
            txtMaTS.setText(""); txtTenTS.setText("");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi nhập liệu!"); }
    }

    private void capPhatTaiSan() {
        int row = tableTaiSan.getSelectedRow();
        if (row == -1) return;
        String maTS = modelTaiSan.getValueAt(row, 0).toString();
        String maNV = txtMaNVSudung.getText().trim();
        if (maNV.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập Mã NV nhận!"); return; }

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE tai_san SET ma_nv_su_dung = ? WHERE ma_ts = ?")) {
            pstmt.setString(1, maNV);
            pstmt.setString(2, maTS);
            pstmt.executeUpdate();
            loadTaiSan();
            parent.ghiNhatKy("Tài sản", "Cấp " + maTS + " cho " + maNV);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void thuHoiTaiSan() {
        int row = tableTaiSan.getSelectedRow();
        if (row == -1) return;
        String maTS = modelTaiSan.getValueAt(row, 0).toString();

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE tai_san SET ma_nv_su_dung = NULL WHERE ma_ts = ?")) {
            pstmt.setString(1, maTS);
            pstmt.executeUpdate();
            loadTaiSan();
            parent.ghiNhatKy("Tài sản", "Thu hồi " + maTS + " về kho");
        } catch (Exception e) { e.printStackTrace(); }
    }
}