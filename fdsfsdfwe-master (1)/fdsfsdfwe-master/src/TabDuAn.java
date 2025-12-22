import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TabDuAn extends JPanel {
    
    private QuanLyNhanVienGUI parent;
    private List<NhanVien> danhSachNV;
    private List<DuAn> danhSachDuAn;

    private JTextField txtMaDuAn, txtTenDuAn;
    private JComboBox<Integer> cmbDoPhucTap;
    private DefaultTableModel modelDuAn;
    private JTable tableDuAn;
    private JComboBox<DuAn> cmbChonDuAn;
    private JTextField txtMaNVThemVaoDuAn;
    private DefaultTableModel modelThanhVienDuAn;
    private JTable tableThanhVienDuAn;

    public TabDuAn(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        this.danhSachNV = parent.danhSachNV;
        this.danhSachDuAn = parent.danhSachDuAn;
        
        setLayout(new BorderLayout());
        
        JPanel crudPanel = new JPanel(new BorderLayout(10, 10));
        crudPanel.setBorder(BorderFactory.createTitledBorder("Quản lý Dự án"));

        JPanel formDuAnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formDuAnPanel.add(new JLabel("Mã Dự án:"));
        txtMaDuAn = new JTextField(10);
        formDuAnPanel.add(txtMaDuAn);
        
        formDuAnPanel.add(new JLabel("Tên Dự án:"));
        txtTenDuAn = new JTextField(20);
        formDuAnPanel.add(txtTenDuAn);
        
        formDuAnPanel.add(new JLabel("Độ phức tạp:"));
        cmbDoPhucTap = new JComboBox<>(new Integer[]{1, 2, 3});
        formDuAnPanel.add(cmbDoPhucTap);
        
        JButton btnThemDuAn = new JButton("Thêm Dự án");
        btnThemDuAn.addActionListener(e -> themDuAn());
        formDuAnPanel.add(btnThemDuAn);
        
        crudPanel.add(formDuAnPanel, BorderLayout.NORTH);
        
        String[] columnsDuAn = {"Mã DA", "Tên Dự án", "Độ phức tạp", "Số lượng NS"};
        modelDuAn = new DefaultTableModel(columnsDuAn, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableDuAn = new JTable(modelDuAn);
        
        tableDuAn.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableDuAn.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableDuAn.getColumnModel().getColumn(2).setPreferredWidth(80);
        tableDuAn.getColumnModel().getColumn(3).setPreferredWidth(100);

        crudPanel.add(new JScrollPane(tableDuAn), BorderLayout.CENTER);
        
        JPanel memberPanel = new JPanel(new BorderLayout(10, 10));
        memberPanel.setBorder(BorderFactory.createTitledBorder("Quản lý Thành viên Dự án"));
        
        JPanel memberControlPanel = new JPanel(new BorderLayout());
        
        JPanel selectDuAnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectDuAnPanel.add(new JLabel("Chọn Dự án:"));
        cmbChonDuAn = new JComboBox<>();
        cmbChonDuAn.addActionListener(e -> locThanhVienTheoDuAn());
        selectDuAnPanel.add(cmbChonDuAn);
        
        JPanel addMemberPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addMemberPanel.add(new JLabel("Nhập Mã NV:"));
        txtMaNVThemVaoDuAn = new JTextField(10);
        addMemberPanel.add(txtMaNVThemVaoDuAn);
        JButton btnThemNVVaoDuAn = new JButton("Thêm Nhân viên");
        btnThemNVVaoDuAn.addActionListener(e -> themNhanVienVaoDuAn());
        addMemberPanel.add(btnThemNVVaoDuAn);

        memberControlPanel.add(selectDuAnPanel, BorderLayout.NORTH);
        memberControlPanel.add(addMemberPanel, BorderLayout.CENTER);
        
        memberPanel.add(memberControlPanel, BorderLayout.NORTH);

        String[] columnsThanhVien = {"Mã NV", "Họ Tên", "Phòng ban"};
        modelThanhVienDuAn = new DefaultTableModel(columnsThanhVien, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableThanhVienDuAn = new JTable(modelThanhVienDuAn);
        memberPanel.add(new JScrollPane(tableThanhVienDuAn), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crudPanel, memberPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
        
        refreshTableDuAn(); 
    }
    
    private void themDuAn() {
        String maDA = txtMaDuAn.getText().trim();
        String tenDA = txtTenDuAn.getText().trim();
        Integer doPhucTap = (Integer) cmbDoPhucTap.getSelectedItem();

        if (maDA.isEmpty() || tenDA.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!");
            return; 
        }
        if (danhSachDuAn.stream().anyMatch(da -> da.getMaDuAn().equals(maDA))) { 
            JOptionPane.showMessageDialog(this, "Mã dự án đã tồn tại!");
            return; 
        }

        String sql = "INSERT INTO du_an(ma_da, ten_da, do_phuc_tap) VALUES(?,?,?)";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, maDA);
            pstmt.setString(2, tenDA);
            pstmt.setInt(3, doPhucTap);
            pstmt.executeUpdate();
            
            DuAn da = new DuAn(maDA, tenDA, doPhucTap);
            danhSachDuAn.add(da);
            
            refreshTableDuAn();
            parent.updateDuAnComboBox();
            
            parent.ghiNhatKy("Thêm Dự án", "Mã: " + maDA);
            JOptionPane.showMessageDialog(this, "Thêm dự án thành công!");
            txtMaDuAn.setText("");
            txtTenDuAn.setText("");
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi Database: " + e.getMessage());
        }
    }

    private void themNhanVienVaoDuAn() {
        DuAn selectedDA = (DuAn) cmbChonDuAn.getSelectedItem();
        String maNV = txtMaNVThemVaoDuAn.getText().trim();

        if (selectedDA == null) { JOptionPane.showMessageDialog(this, "Chưa chọn dự án!"); return; }
        if (maNV.isEmpty()) { JOptionPane.showMessageDialog(this, "Chưa nhập mã NV!"); return; }

        NhanVien nvFound = null;
        for (NhanVien nv : danhSachNV) {
            if (nv.getMaNhanVien().equals(maNV)) {
                nvFound = nv;
                break;
            }
        }
        if (nvFound == null) { JOptionPane.showMessageDialog(this, "Không tìm thấy NV!"); return; }
        if (selectedDA.hasThanhVien(nvFound)) { JOptionPane.showMessageDialog(this, "Nhân viên này đã ở trong dự án!"); return; }

        int diemThuong = selectedDA.getDoPhucTap();
        
        // SQL Transaction
        String sqlInsert = "INSERT INTO phan_cong(ma_da, ma_nv) VALUES(?,?)";
        String sqlUpdateDiem = "UPDATE nhan_vien SET diem_thuong_da = ? WHERE ma_nv = ?";

        try (Connection conn = DatabaseHandler.connect()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmt1 = conn.prepareStatement(sqlInsert)) {
                pstmt1.setString(1, selectedDA.getMaDuAn());
                pstmt1.setString(2, nvFound.getMaNhanVien());
                pstmt1.executeUpdate();
            }

            nvFound.addDiemThuongDuAn(diemThuong);
            try (PreparedStatement pstmt2 = conn.prepareStatement(sqlUpdateDiem)) {
                pstmt2.setInt(1, nvFound.getDiemThuongDuAn());
                pstmt2.setString(2, nvFound.getMaNhanVien());
                pstmt2.executeUpdate();
            }

            conn.commit(); 

            selectedDA.addThanhVien(nvFound);
            
            locThanhVienTheoDuAn();
            
            refreshTableDuAn();
            
            JOptionPane.showMessageDialog(this, "Đã thêm " + nvFound.getHoTen() + " vào dự án.\n(Đã lưu dữ liệu)",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    
            txtMaNVThemVaoDuAn.setText("");
            parent.ghiNhatKy("Phân công Dự án", "NV: " + maNV + " vào DA: " + selectedDA.getMaDuAn());
            
            parent.refreshLuongTable();
            parent.refreshBaoCaoTab();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi Database: " + e.getMessage());
             nvFound.setDiemThuongDuAn(nvFound.getDiemThuongDuAn() - diemThuong);
        }
    }

    private void locThanhVienTheoDuAn() {
        if (modelThanhVienDuAn == null || cmbChonDuAn == null) return;
        
        DuAn selectedDA = (DuAn) cmbChonDuAn.getSelectedItem();
        modelThanhVienDuAn.setRowCount(0);
        
        if (selectedDA == null) return;
        
        for (NhanVien nv : selectedDA.getDanhSachThanhVien()) {
            modelThanhVienDuAn.addRow(new Object[]{
                nv.getMaNhanVien(),
                nv.getHoTen(),
                nv.getPhongBan()
            });
        }
    }
    
    public void refreshTableDuAn() {
        if (modelDuAn == null) return;
        modelDuAn.setRowCount(0);
        for (DuAn da : danhSachDuAn) {
             modelDuAn.addRow(new Object[]{
                 da.getMaDuAn(), 
                 da.getTenDuAn(), 
                 da.getDoPhucTap(),
                 da.getDanhSachThanhVien().size()
             });
        }
    }
    
    public void updateDuAnComboBox() {
        if (cmbChonDuAn == null) return;
        Object selected = cmbChonDuAn.getSelectedItem();
        cmbChonDuAn.removeAllItems();
        for (DuAn da : danhSachDuAn) {
            cmbChonDuAn.addItem(da);
        }
        if (selected != null) {
            for(int i=0; i<cmbChonDuAn.getItemCount(); i++) {
                if(cmbChonDuAn.getItemAt(i).getMaDuAn().equals(((DuAn)selected).getMaDuAn())){
                    cmbChonDuAn.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
}