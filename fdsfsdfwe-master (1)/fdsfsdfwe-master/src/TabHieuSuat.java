import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TabHieuSuat extends JPanel {

    private QuanLyNhanVienGUI parent;
    private List<NhanVien> danhSachNV;

    private JPanel cardPanelHieuSuat;
    private CardLayout cardLayoutHieuSuat;
    private JTextField txtMaNVDiemDanh;
    private JRadioButton radioDiMuon;
    private JRadioButton radioVangMat;
    private ButtonGroup groupDiemDanh;
    private DefaultTableModel modelViPham;
    private JTable tableViPham;
    
    public TabHieuSuat(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        this.danhSachNV = parent.danhSachNV;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Chọn chức năng:"));
        String[] modes = {"Điểm danh", "Đánh giá KPI"};
        JComboBox<String> modeSelector = new JComboBox<>(modes);
        
        add(topPanel, BorderLayout.NORTH);

        cardLayoutHieuSuat = new CardLayout();
        cardPanelHieuSuat = new JPanel(cardLayoutHieuSuat);

        JPanel diemDanhPanel = createDiemDanhPanel();
        JPanel kpiPanel = createPlaceholderPanel("Chức năng Đánh giá KPI sẽ được xây dựng ở đây");
        
        cardPanelHieuSuat.add(diemDanhPanel, "Điểm danh");
        cardPanelHieuSuat.add(kpiPanel, "Đánh giá KPI");

        add(cardPanelHieuSuat, BorderLayout.CENTER);

        modeSelector.addActionListener(e -> {
            String selectedMode = (String) modeSelector.getSelectedItem();
            cardLayoutHieuSuat.show(cardPanelHieuSuat, selectedMode);
        });
    }
    
    private JPanel createDiemDanhPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nhập Mã nhân viên:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtMaNVDiemDanh = new JTextField(15);
        panel.add(txtMaNVDiemDanh, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Lỗi vi phạm:"), gbc);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioDiMuon = new JRadioButton("Đi muộn (+1 điểm)");
        radioVangMat = new JRadioButton("Vắng mặt (+2 điểm)");
        radioDiMuon.setSelected(true);
        groupDiemDanh = new ButtonGroup();
        groupDiemDanh.add(radioDiMuon);
        groupDiemDanh.add(radioVangMat);
        radioPanel.add(radioDiMuon);
        radioPanel.add(radioVangMat);
        gbc.gridx = 1;
        panel.add(radioPanel, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        JButton btnGhiNhanDiemDanh = new JButton("Ghi nhận");
        btnGhiNhanDiemDanh.addActionListener(e -> xuLyGhiNhanDiemDanh());
        panel.add(btnGhiNhanDiemDanh, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0; 
        
        String[] columnNames = {"Mã NV", "Họ Tên", "Lỗi vi phạm"};
        modelViPham = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableViPham = new JTable(modelViPham);
        
        JScrollPane scrollPane = new JScrollPane(tableViPham);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách Vi phạm Đã ghi nhận (trong phiên)"));
        panel.add(scrollPane, gbc);

        return panel;
    }
    
    private void xuLyGhiNhanDiemDanh() {
        String maNV = txtMaNVDiemDanh.getText().trim();
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
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên!"); 
            return; 
        }

        int pointsToAdd = 0;
        String violationType = "";
        if (radioDiMuon.isSelected()) {
            pointsToAdd = 1;
            violationType = "Đi muộn";
        } else if (radioVangMat.isSelected()) {
            pointsToAdd = 2;
            violationType = "Vắng mặt";
        }

        nvFound.addDiemViPham(pointsToAdd);

        //Cập nhật xuống Database
        String sql = "UPDATE nhan_vien SET diem_vi_pham = ? WHERE ma_nv = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, nvFound.getDiemViPham());
            pstmt.setString(2, nvFound.getMaNhanVien());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "Đã ghi nhận " + violationType + " cho nhân viên " + nvFound.getHoTen() + ".\n"
                        + "Tổng điểm vi phạm mới: " + nvFound.getDiemViPham() + " (Đã lưu DB)",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

                modelViPham.addRow(new Object[]{
                    nvFound.getMaNhanVien(),
                    nvFound.getHoTen(),
                    violationType
                });

                parent.ghiNhatKy("Phạt vi phạm", "NV: " + maNV + " - " + violationType);
                
                txtMaNVDiemDanh.setText("");
                radioDiMuon.setSelected(true);
                
                parent.refreshLuongTable();
                parent.refreshBaoCaoTab();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi Database: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.ITALIC, 18));
        panel.add(label);
        return panel;
    }
}