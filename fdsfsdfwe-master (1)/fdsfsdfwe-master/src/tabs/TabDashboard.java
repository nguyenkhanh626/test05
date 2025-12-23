package tabs;

import javax.swing.*;

import MainApp.*;
import dataa.*;
import doituong.*;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TabDashboard extends JPanel {

    private QuanLyNhanVienGUI parent;

    public TabDashboard(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        setLayout(new GridLayout(1, 2, 10, 10)); //Chia đôi màn hình
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    
    public void refreshDashboard() {
        removeAll();
        
        Map<String, Long> nvTheoPB = parent.danhSachNV.stream()
            .collect(Collectors.groupingBy(NhanVien::getPhongBan, Collectors.counting()));
            
        Map<Integer, Long> nvTheoThamNien = parent.danhSachNV.stream()
            .collect(Collectors.groupingBy(NhanVien::getThamNien, Collectors.counting()));

        // Add Biểu đồ
        add(new PieChartPanel("Cơ cấu Nhân sự theo Phòng ban", nvTheoPB));
        add(new BarChartPanel("Phân bổ Nhân sự theo Thâm niên", nvTheoThamNien));
        
        revalidate();
        repaint();
    }

    //CLASS VẼ BIỂU ĐỒ TRÒN
    class PieChartPanel extends JPanel {
        private String title;
        private Map<String, Long> data;
        private Color[] colors = {new Color(65, 105, 225), new Color(255, 69, 0), new Color(34, 139, 34), new Color(255, 215, 0), new Color(138, 43, 226)};

        public PieChartPanel(String title, Map<String, Long> data) {
            this.title = title;
            this.data = data;
            setBorder(BorderFactory.createEtchedBorder());
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(title, 20, 30);

            if (data == null || data.isEmpty()) {
                g2.drawString("Chưa có dữ liệu", 100, 100);
                return;
            }

            long total = data.values().stream().mapToLong(Long::longValue).sum();
            int startAngle = 0;
            int i = 0;
            
            int x = 50, y = 50, w = 200, h = 200;
            int legendY = 60;
            
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                int angle = (int) (entry.getValue() * 360 / total);
                g2.setColor(colors[i % colors.length]);
                g2.fillArc(x, y, w, h, startAngle, angle);
                
                g2.fillRect(300, legendY, 15, 15);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                String percent = String.format("%.1f%%", (entry.getValue() * 100.0 / total));
                g2.drawString(entry.getKey() + " (" + entry.getValue() + " - " + percent + ")", 325, legendY + 12);
                
                startAngle += angle;
                legendY += 25;
                i++;
            }
        }
    }

    //CLASS VẼ BIỂU ĐỒ CỘT
    class BarChartPanel extends JPanel {
        private String title;
        private Map<Integer, Long> data;

        public BarChartPanel(String title, Map<Integer, Long> data) {
            this.title = title;
            this.data = data;
            setBorder(BorderFactory.createEtchedBorder());
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(title, 20, 30);

            if (data == null || data.isEmpty()) return;

            long maxVal = data.values().stream().mapToLong(Long::longValue).max().orElse(1);
            int x = 50, yBase = 250;
            int barWidth = 40;
            int scale = 150;

            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawLine(40, 50, 40, yBase);
            g2.drawLine(40, yBase, 350, yBase);

            for (Map.Entry<Integer, Long> entry : data.entrySet()) {
                int height = (int) (entry.getValue() * scale / maxVal);
                g2.setColor(new Color(70, 130, 180));
                g2.fillRect(x, yBase - height, barWidth, height);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, yBase - height, barWidth, height);
                
                g2.drawString(entry.getValue().toString(), x + 15, yBase - height - 5);
                g2.drawString(entry.getKey() + " năm", x + 5, yBase + 20);
                
                x += 60;
            }
            g2.drawString("Thâm niên", 150, yBase + 40);
        }
    }
}