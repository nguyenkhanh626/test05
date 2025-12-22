import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class TabPhongBan extends JPanel {

    private QuanLyNhanVienGUI parent;
    private List<NhanVien> danhSachNV;
    private List<PhongBan> danhSachPB;

    private JComboBox<PhongBan> cmbChonPhongBan;
    private DefaultTableModel modelNhanVienTheoPB;
    private JTable tableNhanVienTheoPB;

    public TabPhongBan(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        this.danhSachNV = parent.danhSachNV;
        this.danhSachPB = parent.danhSachPB;
        
        setLayout(new BorderLayout(10, 10));
        
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectPanel.add(new JLabel("Chọn phòng ban để xem nhân viên:"));
        cmbChonPhongBan = new JComboBox<>();
        cmbChonPhongBan.addActionListener(e -> locNhanVienTheoPhongBan());
        selectPanel.add(cmbChonPhongBan);
        add(selectPanel, BorderLayout.NORTH);

        String[] columnNamesNV = {"Mã NV", "Họ Tên", "SĐT", "Email", "Ngày sinh", "CCCD", "Thâm niên (năm)"};
        modelNhanVienTheoPB = new DefaultTableModel(columnNamesNV, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableNhanVienTheoPB = new JTable(modelNhanVienTheoPB);
        
        add(new JScrollPane(tableNhanVienTheoPB), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }


    
    public void locNhanVienTheoPhongBan() {
        if (modelNhanVienTheoPB == null || cmbChonPhongBan == null) return;
        
        PhongBan selectedPB = (PhongBan) cmbChonPhongBan.getSelectedItem();
        modelNhanVienTheoPB.setRowCount(0);
        
        if (selectedPB == null) return;
    
        String tenPhongBanChon = selectedPB.getTenPhongBan();
    
        for (NhanVien nv : danhSachNV) {
            if (nv.getPhongBan().equals(tenPhongBanChon)) {
                modelNhanVienTheoPB.addRow(new Object[]{
                    nv.getMaNhanVien(), nv.getHoTen(),
                    nv.getSdt(), nv.getEmail(), nv.getNgaySinh(),
                    nv.getCccd(), nv.getThamNien()
                });
            }
        }
    }
    
    public void updatePhongBanComboBox() {
        if (cmbChonPhongBan == null) return;
        cmbChonPhongBan.removeAllItems();
        for (PhongBan pb : danhSachPB) {
            cmbChonPhongBan.addItem(pb);
        }
    }
}
