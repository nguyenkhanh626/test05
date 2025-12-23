package tabs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import MainApp.*;
import doituong.*;

import java.awt.*;
import java.util.List;

public class TabNhatKy extends JPanel {

    private QuanLyNhanVienGUI parent;
    private DefaultTableModel modelLog;
    private JTable tableLog;

    public TabNhatKy(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("NHẬT KÝ HOẠT ĐỘNG HỆ THỐNG (ADMIN ONLY)");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(Color.RED);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Thời gian", "Người thực hiện", "Hành động", "Chi tiết"};
        modelLog = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableLog = new JTable(modelLog);
        
        tableLog.getColumnModel().getColumn(0).setPreferredWidth(150);
        tableLog.getColumnModel().getColumn(1).setPreferredWidth(100);
        tableLog.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableLog.getColumnModel().getColumn(3).setPreferredWidth(400);

        add(new JScrollPane(tableLog), BorderLayout.CENTER);
        
        JButton btnRefresh = new JButton("Cập nhật nhật ký");
        btnRefresh.addActionListener(e -> refreshLogTable());
        add(btnRefresh, BorderLayout.SOUTH);
    }

    public void refreshLogTable() {
        modelLog.setRowCount(0);

        List<LogEntry> logs = parent.getDanhSachLog();
        
        for (int i = logs.size() - 1; i >= 0; i--) {
            LogEntry log = logs.get(i);
            modelLog.addRow(new Object[]{
                log.getThoiGian(),
                log.getNguoiDung(),
                log.getHanhDong(),
                log.getChiTiet()
            });
        }
    }
}