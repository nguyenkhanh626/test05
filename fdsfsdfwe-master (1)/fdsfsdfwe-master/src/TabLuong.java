import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;

public class TabLuong extends JPanel {

    private QuanLyNhanVienGUI parent;
    private List<NhanVien> danhSachNV;
    private NumberFormat currencyFormatter;

    private DefaultTableModel modelLuong;
    private JTable tableLuong;
    
    public TabLuong(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        this.danhSachNV = parent.danhSachNV;
        this.currencyFormatter = parent.currencyFormatter;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnRefreshLuong = new JButton("Làm mới Bảng lương");
        btnRefreshLuong.addActionListener(e -> refreshLuongTable());
        
        JButton btnXuatExcel = new JButton("Xuất Bảng Lương (Excel)");
        btnXuatExcel.setBackground(new Color(0, 153, 76));
        btnXuatExcel.setForeground(Color.WHITE);
        btnXuatExcel.setFocusPainted(false);
        btnXuatExcel.addActionListener(e -> xuatFileExcel());

        topPanel.add(btnRefreshLuong);
        topPanel.add(btnXuatExcel);
        
        add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {
            "Mã NV", "Họ Tên", "Lương (CB+TN)", 
            "Điểm thưởng DA", "Thưởng Dự án", 
            "Thưởng Chuyên cần", "Điểm Vi phạm", 
            "Tiền Phạt", "Lương Thực nhận"
        };
        modelLuong = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableLuong = new JTable(modelLuong);
        add(new JScrollPane(tableLuong), BorderLayout.CENTER);
    }

    public void refreshLuongTable() {
        if (modelLuong == null) return;

        modelLuong.setRowCount(0);
        
        final long LUONG_CO_BAN = 15_000_000;
        final long THAM_NIEN_BONUS = 5_000_000;
        final long PHAT_VI_PHAM = 500_000;
        final long THUONG_DU_AN_MULTI = 2_000_000;
        final long THUONG_CHUYEN_CAN = 1_000_000;

        for (NhanVien nv : danhSachNV) {
            int soLanTangLuong = nv.getThamNien() / 3;
            long phuCapThamNien = (long) soLanTangLuong * THAM_NIEN_BONUS;
            long luongTruocTru = LUONG_CO_BAN + phuCapThamNien;

            int diemThuongDA = nv.getDiemThuongDuAn();
            long thuongDuAn = (long) diemThuongDA * THUONG_DU_AN_MULTI;

            long thuongChuyenCan = (nv.getDiemViPham() == 0) ? THUONG_CHUYEN_CAN : 0;
            int diemViPham = nv.getDiemViPham();
            long tienPhat = (long) diemViPham * PHAT_VI_PHAM;

            long luongCuoiCung = luongTruocTru + thuongDuAn + thuongChuyenCan - tienPhat;

            modelLuong.addRow(new Object[]{
                nv.getMaNhanVien(),
                nv.getHoTen(),
                currencyFormatter.format(luongTruocTru),
                diemThuongDA,
                currencyFormatter.format(thuongDuAn),
                currencyFormatter.format(thuongChuyenCan),
                diemViPham,
                currencyFormatter.format(tienPhat),
                currencyFormatter.format(luongCuoiCung)
            });
        }
    }

    private void xuatFileExcel() {
        if (tableLuong.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu Bảng lương");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel CSV (*.csv)", "csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            if (!fileToSave.getAbsolutePath().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fileToSave), StandardCharsets.UTF_8))) {
                
                writer.write('\uFEFF'); 

                for (int i = 0; i < modelLuong.getColumnCount(); i++) {
                    writer.write(modelLuong.getColumnName(i));
                    if (i < modelLuong.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();

                for (int row = 0; row < modelLuong.getRowCount(); row++) {
                    for (int col = 0; col < modelLuong.getColumnCount(); col++) {
                        Object value = modelLuong.getValueAt(row, col);
                        String data = (value != null) ? value.toString() : "";
                        
                        data = data.replace(",", "."); 
                        
                        writer.write(data);
                        if (col < modelLuong.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.newLine();
                }

                JOptionPane.showMessageDialog(this, "Xuất file thành công!\nĐường dẫn: " + fileToSave.getAbsolutePath());
                
                try {
                    Desktop.getDesktop().open(fileToSave);
                } catch (Exception ex) {
                    // do nothing
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}