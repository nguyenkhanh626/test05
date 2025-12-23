package tabs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import MainApp.*;
import doituong.*;
import dataa.*;

import java.awt.*;
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
        JButton btnRefreshLuong = new JButton("Tính toán Lương");
        btnRefreshLuong.addActionListener(e -> refreshLuongTable());
        
        JButton btnInPhieu = new JButton("In Phiếu Lương (Chi tiết)");
        btnInPhieu.setBackground(new Color(0, 102, 204));
        btnInPhieu.setForeground(Color.WHITE);
        btnInPhieu.addActionListener(e -> inPhieuLuong());

        topPanel.add(btnRefreshLuong);
        topPanel.add(btnInPhieu);
        add(topPanel, BorderLayout.NORTH);

        //BHXH và Thuế
        String[] columnNames = {
            "Mã NV", "Họ Tên", "Tổng Thu Nhập", 
            "Khấu trừ BHXH (8%)", "Thuế TNCN", "Phạt", "THỰC LĨNH"
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
        
        final long LUONG_CO_BAN = 10_000_000; // Giả định lương cứng mới
        final long PHAT_VI_PHAM = 500_000;

        for (NhanVien nv : danhSachNV) {
            //Thu nhập
            long phuCapThamNien = nv.getThamNien() * 1_000_000L;
            long thuongDA = nv.getDiemThuongDuAn() * 2_000_000L;
            long tongThuNhap = LUONG_CO_BAN + phuCapThamNien + thuongDA;

            //Khấu trừ
            long phat = nv.getDiemViPham() * PHAT_VI_PHAM;
            long bhxh = (long) (tongThuNhap * 0.08); // 8% BHXH
            
            //Tính Thuế TNCN(Giả định >11tr đóng thuế 10% phần dư)
            long thuNhapChiuThue = tongThuNhap - bhxh - 11_000_000;
            long thueTNCN = 0;
            if (thuNhapChiuThue > 0) {
                thueTNCN = (long) (thuNhapChiuThue * 0.1);
            }

            long thucLinh = tongThuNhap - bhxh - thueTNCN - phat;

            modelLuong.addRow(new Object[]{
                nv.getMaNhanVien(), nv.getHoTen(),
                currencyFormatter.format(tongThuNhap),
                currencyFormatter.format(bhxh),
                currencyFormatter.format(thueTNCN),
                currencyFormatter.format(phat),
                currencyFormatter.format(thucLinh)
            });
        }
    }

    private void inPhieuLuong() {
        int r = tableLuong.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Chọn nhân viên cần in phiếu!"); return; }
        
        String maNV = modelLuong.getValueAt(r, 0).toString();
        String hoTen = modelLuong.getValueAt(r, 1).toString();
        String thucLinh = modelLuong.getValueAt(r, 6).toString();
        
        // Tạo nội dung phiếu lương
        StringBuilder sb = new StringBuilder();
        sb.append("========== PHIẾU LƯƠNG THÁNG ==========\n");
        sb.append("Mã NV: ").append(maNV).append("\n");
        sb.append("Họ tên: ").append(hoTen).append("\n");
        sb.append("---------------------------------------\n");
        sb.append("Tổng thu nhập:  ").append(modelLuong.getValueAt(r, 2)).append("\n");
        sb.append("(-) BHXH (8%):  ").append(modelLuong.getValueAt(r, 3)).append("\n");
        sb.append("(-) Thuế TNCN:  ").append(modelLuong.getValueAt(r, 4)).append("\n");
        sb.append("(-) Vi phạm:    ").append(modelLuong.getValueAt(r, 5)).append("\n");
        sb.append("---------------------------------------\n");
        sb.append("THỰC LĨNH:      ").append(thucLinh).append("\n");
        sb.append("=======================================\n");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Phiếu Lương Chi Tiết", JOptionPane.INFORMATION_MESSAGE);
    }
}