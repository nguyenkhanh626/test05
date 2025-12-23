package tabs;

import javax.swing.*;

import MainApp.*;
import doituong.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TabHeThong extends JPanel {

    private QuanLyNhanVienGUI parent;
    private QuanLyTaiKhoan quanLyTaiKhoan;

    
    private JPasswordField txtPassCu;
    private JPasswordField txtPassMoi;
    private JPasswordField txtPassXacNhan;

    
    private static final String DB_SOURCE = "quanlynhansu.db";

    public TabHeThong(QuanLyNhanVienGUI parent) {
        this.parent = parent;
        this.quanLyTaiKhoan = new QuanLyTaiKhoan();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //BACKUP/RESTORE
        JPanel pnlData = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlData.setBorder(BorderFactory.createTitledBorder("Quản trị Cơ sở dữ liệu"));
        pnlData.setPreferredSize(new Dimension(0, 150));

        JPanel pnlBackup = new JPanel(new GridBagLayout());
        JButton btnBackup = new JButton("Sao lưu Dữ liệu (Backup)");
        btnBackup.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        btnBackup.setBackground(new Color(0, 102, 204));
        btnBackup.setForeground(Color.WHITE);
        btnBackup.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel lblBackupInfo = new JLabel("<html><center>Sao chép toàn bộ dữ liệu hiện tại<br/>ra file dự phòng an toàn.</center></html>");
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; pnlBackup.add(btnBackup, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.insets = new Insets(10,0,0,0); pnlBackup.add(lblBackupInfo, gbc);

        JPanel pnlRestore = new JPanel(new GridBagLayout());
        JButton btnRestore = new JButton("Phục hồi Dữ liệu (Restore)");
        btnRestore.setIcon(UIManager.getIcon("FileView.computerIcon"));
        btnRestore.setBackground(new Color(204, 0, 0));
        btnRestore.setForeground(Color.WHITE);
        btnRestore.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel lblRestoreInfo = new JLabel("<html><center>Khôi phục dữ liệu từ file đã lưu.<br/>(Cảnh báo: Dữ liệu hiện tại sẽ mất)</center></html>");
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0,0,0,0); pnlRestore.add(btnRestore, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.insets = new Insets(10,0,0,0); pnlRestore.add(lblRestoreInfo, gbc);

        pnlData.add(pnlBackup);
        pnlData.add(pnlRestore);

        //ĐỔI MẬT KHẨU
        JPanel pnlSecurity = new JPanel(new BorderLayout());
        pnlSecurity.setBorder(BorderFactory.createTitledBorder("Bảo mật & Tài khoản"));
        
        JPanel formPass = new JPanel(new GridBagLayout());
        GridBagConstraints gbcPass = new GridBagConstraints();
        gbcPass.insets = new Insets(5, 5, 5, 5);
        gbcPass.anchor = GridBagConstraints.WEST;

        gbcPass.gridx = 0; gbcPass.gridy = 0; formPass.add(new JLabel("Mật khẩu hiện tại:"), gbcPass);
        txtPassCu = new JPasswordField(20); gbcPass.gridx = 1; formPass.add(txtPassCu, gbcPass);

        gbcPass.gridx = 0; gbcPass.gridy = 1; formPass.add(new JLabel("Mật khẩu mới:"), gbcPass);
        txtPassMoi = new JPasswordField(20); gbcPass.gridx = 1; formPass.add(txtPassMoi, gbcPass);

        gbcPass.gridx = 0; gbcPass.gridy = 2; formPass.add(new JLabel("Nhập lại mật khẩu mới:"), gbcPass);
        txtPassXacNhan = new JPasswordField(20); gbcPass.gridx = 1; formPass.add(txtPassXacNhan, gbcPass);

        JButton btnDoiPass = new JButton("Cập nhật Mật khẩu");
        gbcPass.gridx = 1; gbcPass.gridy = 3; gbcPass.anchor = GridBagConstraints.EAST;
        formPass.add(btnDoiPass, gbcPass);

        pnlSecurity.add(formPass, BorderLayout.CENTER);
        
        //THÔNG TIN HỆ THỐNG (BOTTOM)
        JPanel pnlInfo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        String osInfo = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")";
        String javaInfo = System.getProperty("java.version");
        JLabel lblSys = new JLabel("Hệ thống: " + osInfo + " | Java: " + javaInfo);
        lblSys.setForeground(Color.GRAY);
        pnlInfo.add(lblSys);

        JPanel mainCenter = new JPanel(new BorderLayout(10, 10));
        mainCenter.add(pnlData, BorderLayout.NORTH);
        mainCenter.add(pnlSecurity, BorderLayout.CENTER);

        add(mainCenter, BorderLayout.CENTER);
        add(pnlInfo, BorderLayout.SOUTH);

        btnBackup.addActionListener(e -> xuLyBackup());
        btnRestore.addActionListener(e -> xuLyRestore());
        btnDoiPass.addActionListener(e -> xuLyDoiMatKhau());
    }

    //Yêu cầu xác thực mật khẩu trước khi thực hiện hành động nhạy cảm
    private boolean yeuCauXacThuc() {
        String currentUser = parent.getCurrentUser();
        if (currentUser == null || currentUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không xác định được phiên đăng nhập!");
            return false;
        }

        JPasswordField pf = new JPasswordField();
        int okCxl = JOptionPane.showConfirmDialog(this, pf, 
            "Nhập mật khẩu của [" + currentUser + "] để tiếp tục:", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (okCxl == JOptionPane.OK_OPTION) {
            String password = new String(pf.getPassword());
            //Tái sử dụng hàm đăng nhập để kiểm tra pass
            if (quanLyTaiKhoan.dangNhap(currentUser, password) != null) {
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Mật khẩu không đúng! Từ chối truy cập.", "Cảnh báo bảo mật", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    private void xuLyBackup() {
        //Thêm lớp bảo mật
        if (!yeuCauXacThuc()) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file Backup");
        fileChooser.setSelectedFile(new File("backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".db"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileDest = fileChooser.getSelectedFile();
            File fileSource = new File(DB_SOURCE);
            
            try {
                if (!fileSource.exists()) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy file database gốc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Files.copy(fileSource.toPath(), fileDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Sao lưu thành công!\n" + fileDest.getAbsolutePath());
                parent.ghiNhatKy("Hệ thống", "Backup database thành công");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi sao lưu: " + ex.getMessage());
            }
        }
    }

    private void xuLyRestore() {
        //Thêm lớp bảo mật
        if (!yeuCauXacThuc()) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
            "CẢNH BÁO: Dữ liệu hiện tại sẽ bị ghi đè hoàn toàn bởi file backup.\nBạn có chắc chắn muốn tiếp tục?", 
            "Xác nhận phục hồi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm != JOptionPane.YES_OPTION) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Backup để phục hồi");
        
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileSource = fileChooser.getSelectedFile();
            File fileDest = new File(DB_SOURCE);
            
            try {
                //Copy đè file
                Files.copy(fileSource.toPath(), fileDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                JOptionPane.showMessageDialog(this, "Phục hồi thành công! Vui lòng khởi động lại ứng dụng để áp dụng dữ liệu mới.");
                parent.ghiNhatKy("Hệ thống", "Restore database từ: " + fileSource.getName());
                
                //Tự động tắt ứng dụng
                System.exit(0);
                
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi phục hồi: " + ex.getMessage());
            }
        }
    }

    private void xuLyDoiMatKhau() {
        String passCu = new String(txtPassCu.getPassword());
        String passMoi = new String(txtPassMoi.getPassword());
        String xacNhan = new String(txtPassXacNhan.getPassword());
        
        if (passCu.isEmpty() || passMoi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!"); return;
        }
        
        if (!passMoi.equals(xacNhan)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
        }
        
        // Lấy username hiện tại từ Parent GUI
        String currentUser = parent.getCurrentUser();
        if (currentUser == null || currentUser.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Không xác định được người dùng hiện tại!"); return;
        }

        boolean ketQua = quanLyTaiKhoan.doiMatKhau(currentUser, passCu, passMoi);
        if (ketQua) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
            txtPassCu.setText(""); txtPassMoi.setText(""); txtPassXacNhan.setText("");
            parent.ghiNhatKy("Bảo mật", "Đổi mật khẩu thành công");
        } else {
            JOptionPane.showMessageDialog(this, "Mật khẩu cũ không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}