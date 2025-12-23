package tabs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import MainApp.*;
import dataa.*;
import doituong.*;

import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TabHieuSuat extends JPanel {

    private QuanLyNhanVienGUI parent;
    private List<NhanVien> danhSachNV;

    private JComboBox<String> cmbCaLamViec;
    private JTextField txtMaNVChamCong;
    private DefaultTableModel modelChamCong;
    private JTable tableChamCong;
    private JLabel lblStatusCheckIn;
    
    private JButton btnCheckIn;
    private JButton btnCheckOut;
    private JButton btnRefresh;

    private JTextField txtMaNVNghi;
    private JTextField txtTuNgay, txtDenNgay;
    private JTextArea txtLyDo;
    private DefaultTableModel modelNghiPhep;
    private JTable tableNghiPhep;

    private JTextField txtMaNVViPham;
    private JRadioButton radioDiMuon;
    private JRadioButton radioKhongPhep; 
    private ButtonGroup groupViPham;
    private DefaultTableModel modelLichSuViPham;
    private JTable tableLichSuViPham;

    public TabHieuSuat(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        this.danhSachNV = parent.danhSachNV; 
        setLayout(new BorderLayout());
        
        JTabbedPane tabSub = new JTabbedPane();
        
        JPanel pnlChamCong = createPanelChamCong();
        JPanel pnlNghiPhep = createPanelNghiPhep();
        JPanel pnlViPham = createPanelViPham(); 
        
        tabSub.addTab("Chấm công (Check-in/Out)", null, pnlChamCong, "Ghi nhận giờ vào ra");
        tabSub.addTab("Quản lý Nghỉ phép", null, pnlNghiPhep, "Duyệt đơn nghỉ phép");
        tabSub.addTab("Xử lý Vi phạm & Kỷ luật", null, pnlViPham, "Trừ điểm đi muộn/không phép");
        
        add(tabSub, BorderLayout.CENTER);
    }

    //CHẤM CÔNG
    
    private JPanel createPanelChamCong() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Ghi nhận Chấm công"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("Chọn Ca làm việc:"), gbc);
        gbc.gridx = 1; 
        cmbCaLamViec = new JComboBox<>();
        loadDanhSachCa();
        inputPanel.add(cmbCaLamViec, gbc);

        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("Mã Nhân viên:"), gbc);
        gbc.gridx = 1; 
        txtMaNVChamCong = new JTextField(15);
        
        txtMaNVChamCong.addActionListener(e -> xuLyCheckIn());
        inputPanel.add(txtMaNVChamCong, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnCheckIn = new JButton("CHECK-IN (Vào)");
        btnCheckIn.setBackground(new Color(0, 153, 76)); btnCheckIn.setForeground(Color.WHITE);
        
        btnCheckOut = new JButton("CHECK-OUT (Ra)");
        btnCheckOut.setBackground(new Color(204, 0, 0)); btnCheckOut.setForeground(Color.WHITE);

        btnPanel.add(btnCheckIn);
        btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(btnCheckOut);
        
        gbc.gridx = 1; gbc.gridy = 2; inputPanel.add(btnPanel, gbc);

        lblStatusCheckIn = new JLabel("Vui lòng nhập Mã NV để chấm công.");
        lblStatusCheckIn.setFont(new Font("Arial", Font.ITALIC, 12));
        lblStatusCheckIn.setForeground(Color.BLUE);
        gbc.gridx = 1; gbc.gridy = 3; inputPanel.add(lblStatusCheckIn, gbc);

        panel.add(inputPanel, BorderLayout.NORTH);

        //Bảng dữ liệu
        String[] cols = {"ID", "Ngày", "Mã NV", "Ca", "Giờ Vào", "Giờ Ra"};
        modelChamCong = new DefaultTableModel(cols, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableChamCong = new JTable(modelChamCong);
        panel.add(new JScrollPane(tableChamCong), BorderLayout.CENTER);
        
        btnRefresh = new JButton("Tải lại danh sách hôm nay");
        btnRefresh.addActionListener(e -> loadBangChamCongHienTai());
        panel.add(btnRefresh, BorderLayout.SOUTH);

        btnCheckIn.addActionListener(e -> xuLyCheckIn());
        btnCheckOut.addActionListener(e -> xuLyCheckOut());

        loadBangChamCongHienTai(); 
        return panel;
    }

    private void loadDanhSachCa() {
        if(cmbCaLamViec == null) return;
        cmbCaLamViec.removeAllItems();
        new Thread(() -> {
            try (Connection conn = DatabaseHandler.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM ca_lam_viec")) {
                
                while (rs.next()) {
                    String item = rs.getInt("id") + " - " + rs.getString("ten_ca") 
                                + " (" + rs.getString("gio_bat_dau") + "-" + rs.getString("gio_ket_thuc") + ")";
                    
                    SwingUtilities.invokeLater(() -> cmbCaLamViec.addItem(item));
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }).start();
    }

    private void xuLyCheckIn() {
        String maNV = txtMaNVChamCong.getText().trim();
        if (maNV.isEmpty()) { JOptionPane.showMessageDialog(this, "Chưa nhập Mã NV!"); return; }
        
        if (cmbCaLamViec.getSelectedItem() == null) return;
        String caInfo = (String) cmbCaLamViec.getSelectedItem();
        int maCa = Integer.parseInt(caInfo.split(" - ")[0]); 
        
        btnCheckIn.setEnabled(false);
        btnCheckIn.setText("Đang xử lý...");
        lblStatusCheckIn.setText("⏳ Đang kết nối CSDL...");
        lblStatusCheckIn.setForeground(Color.BLUE);

        new Thread(() -> {
            String ngayHomNay = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            String gioHienTai = new SimpleDateFormat("HH:mm:ss").format(new Date());

            String checkSql = "SELECT COUNT(*) FROM cham_cong WHERE ma_nv=? AND ngay_lam_viec=? AND ma_ca=?";
            String insertSql = "INSERT INTO cham_cong(ma_nv, ngay_lam_viec, ma_ca, gio_vao) VALUES(?,?,?,?)";

            try (Connection conn = DatabaseHandler.connect();
                 PreparedStatement pCheck = conn.prepareStatement(checkSql);
                 PreparedStatement pInsert = conn.prepareStatement(insertSql)) {
                
                pCheck.setString(1, maNV); pCheck.setString(2, ngayHomNay); pCheck.setInt(3, maCa);
                ResultSet rs = pCheck.executeQuery();
                rs.next();
                boolean daCheckIn = rs.getInt(1) > 0;
                rs.close();

                SwingUtilities.invokeLater(() -> {
                    if (daCheckIn) {
                        lblStatusCheckIn.setText("Lỗi: NV " + maNV + " đã Check-in ca này rồi!");
                        lblStatusCheckIn.setForeground(Color.RED);
                    } else {
                        try {
                            lblStatusCheckIn.setText("Đã Check-in NV " + maNV + " lúc " + gioHienTai);
                            lblStatusCheckIn.setForeground(new Color(0, 100, 0));
                            parent.ghiNhatKy("Check-in", "NV: " + maNV + ", Ca: " + maCa);
                            txtMaNVChamCong.setText(""); 
                            txtMaNVChamCong.requestFocus(); 
                            loadBangChamCongHienTai();
                        } catch (Exception ex) {}
                    }
                });

                if (!daCheckIn) {
                    pInsert.setString(1, maNV);
                    pInsert.setString(2, ngayHomNay);
                    pInsert.setInt(3, maCa);
                    pInsert.setString(4, gioHienTai);
                    pInsert.executeUpdate();
                    
                    SwingUtilities.invokeLater(() -> {
                        lblStatusCheckIn.setText("Check-in thành công: " + maNV + " lúc " + gioHienTai);
                        lblStatusCheckIn.setForeground(new Color(0, 100, 0));
                        parent.ghiNhatKy("Check-in", "NV: " + maNV + ", Ca: " + maCa);
                        txtMaNVChamCong.setText("");
                        txtMaNVChamCong.requestFocus();
                        
                        loadBangChamCongHienTai();
                    });
                }

            } catch (SQLException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    lblStatusCheckIn.setText("Lỗi DB: " + e.getMessage());
                    lblStatusCheckIn.setForeground(Color.RED);
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    btnCheckIn.setEnabled(true);
                    btnCheckIn.setText("CHECK-IN (Vào)");
                });
            }
        }).start();
    }

    private void xuLyCheckOut() {
        String maNV = txtMaNVChamCong.getText().trim();
        if (maNV.isEmpty()) { JOptionPane.showMessageDialog(this, "Chưa nhập Mã NV!"); return; }

        if (cmbCaLamViec.getSelectedItem() == null) return;
        String caInfo = (String) cmbCaLamViec.getSelectedItem();
        int maCa = Integer.parseInt(caInfo.split(" - ")[0]);

        btnCheckOut.setEnabled(false);
        btnCheckOut.setText("Đang xử lý...");

        new Thread(() -> {
            String ngayHomNay = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            String gioHienTai = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String updateSql = "UPDATE cham_cong SET gio_ra = ? WHERE ma_nv=? AND ngay_lam_viec=? AND ma_ca=? AND gio_ra IS NULL";

            try (Connection conn = DatabaseHandler.connect();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                
                pstmt.setString(1, gioHienTai);
                pstmt.setString(2, maNV);
                pstmt.setString(3, ngayHomNay);
                pstmt.setInt(4, maCa);
                
                int rows = pstmt.executeUpdate();
                
                SwingUtilities.invokeLater(() -> {
                    if (rows > 0) {
                        lblStatusCheckIn.setText("Check-out thành công: " + maNV + " lúc " + gioHienTai);
                        lblStatusCheckIn.setForeground(new Color(0, 100, 0));
                        parent.ghiNhatKy("Check-out", "NV: " + maNV + ", Ca: " + maCa);
                        txtMaNVChamCong.setText("");
                        txtMaNVChamCong.requestFocus();
                        loadBangChamCongHienTai();
                    } else {
                        lblStatusCheckIn.setText("Không tìm thấy bản ghi Check-in để Check-out!");
                        lblStatusCheckIn.setForeground(Color.RED);
                    }
                });

            } catch (SQLException e) { 
                e.printStackTrace(); 
                SwingUtilities.invokeLater(() -> lblStatusCheckIn.setText("Lỗi DB: " + e.getMessage()));
            } finally {
                SwingUtilities.invokeLater(() -> {
                    btnCheckOut.setEnabled(true);
                    btnCheckOut.setText("CHECK-OUT (Ra)");
                });
            }
        }).start();
    }

    private void loadBangChamCongHienTai() {
        if(modelChamCong == null) return;
        
        if (btnRefresh != null) {
            btnRefresh.setEnabled(false);
            btnRefresh.setText("Đang tải...");
        }

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> modelChamCong.setRowCount(0));

            String ngayHomNay = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            String sql = "SELECT * FROM cham_cong WHERE ngay_lam_viec = ? ORDER BY id DESC";
            
            try (Connection conn = DatabaseHandler.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, ngayHomNay);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()) {
                    Object[] rowData = {
                        rs.getInt("id"),
                        rs.getString("ngay_lam_viec"),
                        rs.getString("ma_nv"),
                        rs.getInt("ma_ca"),
                        rs.getString("gio_vao"),
                        rs.getString("gio_ra")
                    };
                    SwingUtilities.invokeLater(() -> modelChamCong.addRow(rowData));
                }
            } catch (SQLException e) { e.printStackTrace(); }
            finally {
                SwingUtilities.invokeLater(() -> {
                    if (btnRefresh != null) {
                        btnRefresh.setEnabled(true);
                        btnRefresh.setText("Tải lại danh sách hôm nay");
                    }
                });
            }
        }).start();
    }

    //NGHỈ PHÉP

    private JPanel createPanelNghiPhep() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Form xin nghỉ
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Tạo đơn xin nghỉ phép"));
        
        formPanel.add(new JLabel("Mã Nhân viên:"));
        txtMaNVNghi = new JTextField(); formPanel.add(txtMaNVNghi);
        
        formPanel.add(new JLabel("Từ ngày (dd/MM/yyyy):"));
        txtTuNgay = new JTextField(); formPanel.add(txtTuNgay);
        
        formPanel.add(new JLabel("Đến ngày (dd/MM/yyyy):"));
        txtDenNgay = new JTextField(); formPanel.add(txtDenNgay);
        
        formPanel.add(new JLabel("Lý do:"));
        txtLyDo = new JTextArea(3, 20); 
        JScrollPane scrollLyDo = new JScrollPane(txtLyDo);
        formPanel.add(scrollLyDo);
        
        JButton btnGuiDon = new JButton("Gửi Đơn");
        btnGuiDon.addActionListener(e -> guiDonNghiPhep());
        
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(btnGuiDon, BorderLayout.SOUTH);
        
        panel.add(topContainer, BorderLayout.NORTH);

        //Bảng danh sách đơn
        String[] cols = {"ID", "Mã NV", "Từ ngày", "Đến ngày", "Lý do", "Trạng thái"};
        modelNghiPhep = new DefaultTableModel(cols, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableNghiPhep = new JTable(modelNghiPhep);
        panel.add(new JScrollPane(tableNghiPhep), BorderLayout.CENTER);
        
        JButton btnLoadDon = new JButton("Làm mới danh sách");
        btnLoadDon.addActionListener(e -> loadDanhSachDonNghi());
        panel.add(btnLoadDon, BorderLayout.SOUTH);

        loadDanhSachDonNghi();
        return panel;
    }

    private void guiDonNghiPhep() {
        String maNV = txtMaNVNghi.getText().trim();
        String tuNgay = txtTuNgay.getText().trim();
        String denNgay = txtDenNgay.getText().trim();
        String lyDo = txtLyDo.getText().trim();

        if (maNV.isEmpty() || tuNgay.isEmpty() || denNgay.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!"); return;
        }

        String sql = "INSERT INTO don_nghi_phep(ma_nv, tu_ngay, den_ngay, ly_do, trang_thai, ngay_tao) VALUES(?,?,?,?,?,?)";
        String ngayTao = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, maNV);
            pstmt.setString(2, tuNgay);
            pstmt.setString(3, denNgay);
            pstmt.setString(4, lyDo);
            pstmt.setString(5, "Chờ duyệt");
            pstmt.setString(6, ngayTao);
            
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Gửi đơn thành công!");
            txtMaNVNghi.setText(""); txtLyDo.setText("");
            loadDanhSachDonNghi();
            parent.ghiNhatKy("Xin nghỉ phép", "NV: " + maNV + " (" + tuNgay + " -> " + denNgay + ")");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi Database: " + e.getMessage());
        }
    }

    private void loadDanhSachDonNghi() {
        if(modelNghiPhep == null) return;
        modelNghiPhep.setRowCount(0);
        String sql = "SELECT * FROM don_nghi_phep ORDER BY id DESC";
        try (Connection conn = DatabaseHandler.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while(rs.next()) {
                modelNghiPhep.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("ma_nv"),
                    rs.getString("tu_ngay"),
                    rs.getString("den_ngay"),
                    rs.getString("ly_do"),
                    rs.getString("trang_thai")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    //XỬ LÝ VI PHẠM

    private JPanel createPanelViPham() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        //Form nhập liệu phạt
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nhập Mã nhân viên vi phạm:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtMaNVViPham = new JTextField(15);
        panel.add(txtMaNVViPham, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Loại vi phạm:"), gbc);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioDiMuon = new JRadioButton("Đi muộn (+1 điểm phạt)");
        radioKhongPhep = new JRadioButton("Nghỉ không phép (+2 điểm phạt)");
        radioDiMuon.setSelected(true);
        
        groupViPham = new ButtonGroup();
        groupViPham.add(radioDiMuon);
        groupViPham.add(radioKhongPhep);
        
        radioPanel.add(radioDiMuon);
        radioPanel.add(radioKhongPhep);
        
        gbc.gridx = 1;
        panel.add(radioPanel, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        JButton btnPhat = new JButton("Ghi nhận Vi phạm");
        btnPhat.setBackground(Color.RED);
        btnPhat.setForeground(Color.WHITE);
        btnPhat.addActionListener(e -> xuLyGhiNhanViPham());
        panel.add(btnPhat, gbc);
        
        //Bảng lịch sử phạt
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0; 
        
        String[] columnNames = {"Mã NV", "Họ Tên", "Lỗi vi phạm", "Điểm cộng thêm"};
        modelLichSuViPham = new DefaultTableModel(columnNames, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableLichSuViPham = new JTable(modelLichSuViPham);
        
        JScrollPane scrollPane = new JScrollPane(tableLichSuViPham);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách Vi phạm vừa ghi nhận"));
        panel.add(scrollPane, gbc);

        return panel;
    }

    private void xuLyGhiNhanViPham() {
        String maNV = txtMaNVViPham.getText().trim();
        if (maNV.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã NV!"); 
            return; 
        }

        NhanVien nvFound = null;
        for (NhanVien nv : danhSachNV) {
            if (nv.getMaNhanVien().equals(maNV)) {
                nvFound = nv;
                break;
            }
        }
        if (nvFound == null) { 
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên trong hệ thống!"); 
            return; 
        }

        int pointsToAdd = 0;
        String violationType = "";
        
        if (radioDiMuon.isSelected()) {
            pointsToAdd = 1;
            violationType = "Đi muộn";
        } else if (radioKhongPhep.isSelected()) {
            pointsToAdd = 2;
            violationType = "Nghỉ không phép";
        }

        nvFound.addDiemViPham(pointsToAdd);

        String sql = "UPDATE nhan_vien SET diem_vi_pham = ? WHERE ma_nv = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, nvFound.getDiemViPham());
            pstmt.setString(2, nvFound.getMaNhanVien());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "Đã phạt lỗi [" + violationType + "] cho nhân viên " + nvFound.getHoTen() + ".\n"
                        + "Tổng điểm vi phạm hiện tại: " + nvFound.getDiemViPham(),
                        "Ghi nhận thành công", JOptionPane.INFORMATION_MESSAGE);

                modelLichSuViPham.addRow(new Object[]{
                    nvFound.getMaNhanVien(),
                    nvFound.getHoTen(),
                    violationType,
                    "+" + pointsToAdd
                });

                parent.ghiNhatKy("Phạt vi phạm", "NV: " + maNV + " - " + violationType);
                txtMaNVViPham.setText("");
                radioDiMuon.setSelected(true);
                
                parent.refreshLuongTable();
                parent.refreshBaoCaoTab();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi Database: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}