import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuanLyNhanVienGUI extends JFrame {

    
    List<NhanVien> danhSachNV;
    List<PhongBan> danhSachPB;
    List<DuAn> danhSachDuAn;
    private List<LogEntry> danhSachLog;

    
    private JTabbedPane tabbedPane;
    private TabNhanVien tabNhanVien;
    private TabPhongBan tabPhongBan;
    private TabDuAn tabDuAn;
    private TabHieuSuat tabHieuSuat;
    private TabLuong tabLuong;
    private TabBaoCao tabBaoCao;
    private TabNhatKy tabNhatKy;

    NumberFormat currencyFormatter;
    private QuanLyTaiKhoan quanLyTaiKhoan; 
    private String currentUser = ""; 
    
    
    private JButton btnTaoTaiKhoan; 
    private JLabel lblXinChao; 
    private JLabel lblThoiGianPhien; 
    private Timer sessionTimer;      
    private long startSessionTime;   

    public QuanLyNhanVienGUI() {
        
        DatabaseHandler.createNewDatabase();
        
        setTitle("Phần mềm Quản lý Nhân sự (Connected SQLite)");
        setSize(1250, 750); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
        quanLyTaiKhoan = new QuanLyTaiKhoan();
        
        
        danhSachNV = new ArrayList<>();
        danhSachPB = new ArrayList<>();
        danhSachDuAn = new ArrayList<>();
        danhSachLog = new ArrayList<>();
        
        loadDataFromDB();
        
        ghiNhatKy("Khởi động", "Ứng dụng đã được bật");

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabNhanVien = new TabNhanVien(this);
        tabPhongBan = new TabPhongBan(this);
        tabDuAn = new TabDuAn(this);
        tabLuong = new TabLuong(this);
        tabHieuSuat = new TabHieuSuat(this);
        tabBaoCao = new TabBaoCao(this);
        tabNhatKy = new TabNhatKy(this);

        tabbedPane.addTab("Quản lý Nhân viên", null, tabNhanVien, "Quản lý thông tin nhân viên");
        tabbedPane.addTab("Xem theo Phòng ban", null, tabPhongBan, "Xem nhân viên theo phòng ban");
        tabbedPane.addTab("Quản lý Dự án", null, tabDuAn, "Quản lý các dự án");
        tabbedPane.addTab("Quản lý Lương", null, tabLuong, "Xem bảng lương nhân viên");
        tabbedPane.addTab("Quản lý Hiệu suất", null, tabHieuSuat, "Quản lý và đánh giá hiệu suất");
        tabbedPane.addTab("Báo cáo", null, tabBaoCao, "Báo cáo và Thống kê");

        add(tabbedPane, BorderLayout.CENTER);
        refreshAllTabs();
    }

    //LOAD DỮ LIỆU, NẠP MẪU
    private void loadDataFromDB() {
        try (Connection conn = DatabaseHandler.connect();
             Statement stmt = conn.createStatement()) {
            
            
            ResultSet rsCheckPB = stmt.executeQuery("SELECT COUNT(*) FROM phong_ban");
            rsCheckPB.next();
            if (rsCheckPB.getInt(1) == 0) {
                System.out.println("⚠️ Bảng Phòng ban trống. Đang nạp dữ liệu mẫu...");
                stmt.execute("INSERT INTO phong_ban VALUES ('KT', 'Kỹ thuật'), ('KD', 'Kinh doanh'), ('NS', 'Nhân sự')");
            }
            ResultSet rsPB = stmt.executeQuery("SELECT * FROM phong_ban");
            while (rsPB.next()) {
                danhSachPB.add(new PhongBan(rsPB.getString("ma_pb"), rsPB.getString("ten_pb")));
            }

            
            ResultSet rsCheckDA = stmt.executeQuery("SELECT COUNT(*) FROM du_an");
            rsCheckDA.next();
            if (rsCheckDA.getInt(1) == 0) {
                System.out.println("⚠️ Bảng Dự án trống. Đang nạp dữ liệu mẫu...");
                stmt.execute("INSERT INTO du_an (ma_da, ten_da, do_phuc_tap) VALUES " +
                        "('DA01', 'Website Thương mại điện tử', 3)," +
                        "('DA02', 'Hệ thống CRM nội bộ', 2)");
            }
            ResultSet rsDA = stmt.executeQuery("SELECT * FROM du_an");
            while (rsDA.next()) {
                danhSachDuAn.add(new DuAn(rsDA.getString("ma_da"), rsDA.getString("ten_da"), rsDA.getInt("do_phuc_tap")));
            }

           
            ResultSet rsCheckNV = stmt.executeQuery("SELECT COUNT(*) FROM nhan_vien");
            rsCheckNV.next();
            if (rsCheckNV.getInt(1) == 0) {
                System.out.println("⚠️ Bảng Nhân viên trống. Đang nạp dữ liệu mẫu...");
                stmt.execute("INSERT INTO nhan_vien (ma_nv, ho_ten, phong_ban, sdt, email, ngay_sinh, cccd, tham_nien) VALUES " +
                        "('NV001', 'Nguyễn Văn A', 'Kỹ thuật', '0900111222', 'a.nguyen@example.com', '01/01/1990', '123456789', 5)," +
                        "('NV002', 'Trần Thị B', 'Kinh doanh', '0900333444', 'b.tran@example.com', '02/02/1992', '987654321', 3)," +
                        "('NV003', 'Lê Văn C', 'Nhân sự', '0900555666', 'c.le@example.com', '03/03/1995', '111222333', 1)," +
                        "('NV004', 'Phạm Văn D', 'Kỹ thuật', '0900777888', 'd.pham@example.com', '04/04/1998', '444555666', 2)");
            }
            ResultSet rsNV = stmt.executeQuery("SELECT * FROM nhan_vien");
            while (rsNV.next()) {
                NhanVien nv = new NhanVien(
                    rsNV.getString("ma_nv"),
                    rsNV.getString("ho_ten"),
                    rsNV.getString("phong_ban"),
                    rsNV.getString("sdt"),
                    rsNV.getString("email"),
                    rsNV.getString("ngay_sinh"),
                    rsNV.getString("cccd"),
                    rsNV.getInt("tham_nien")
                );
                nv.setDiemViPham(rsNV.getInt("diem_vi_pham"));
                nv.setDiemThuongDuAn(rsNV.getInt("diem_thuong_da"));
                danhSachNV.add(nv);
            }

            
            ResultSet rsPC = stmt.executeQuery("SELECT * FROM phan_cong");
            while (rsPC.next()) {
                String maDA = rsPC.getString("ma_da");
                String maNV = rsPC.getString("ma_nv");

                // Tìm đối tượng DuAn và NhanVien tương ứng trong List
                DuAn da = danhSachDuAn.stream()
                        .filter(d -> d.getMaDuAn().equals(maDA))
                        .findFirst().orElse(null);
                
                NhanVien nv = danhSachNV.stream()
                        .filter(n -> n.getMaNhanVien().equals(maNV))
                        .findFirst().orElse(null);

                
                if (da != null && nv != null) {
                    da.addThanhVien(nv);
                }
            }

            
            ResultSet rsLog = stmt.executeQuery("SELECT * FROM nhat_ky ORDER BY id DESC LIMIT 50");
            while (rsLog.next()) {
                 danhSachLog.add(new LogEntry(
                     rsLog.getString("nguoi_dung"), 
                     rsLog.getString("hanh_dong"), 
                     rsLog.getString("chi_tiet")
                 ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }
    
    public void ghiNhatKy(String hanhDong, String chiTiet) {
        String user = currentUser.isEmpty() ? "Khách/System" : currentUser;
        
        LogEntry log = new LogEntry(user, hanhDong, chiTiet);
        danhSachLog.add(log);
        
        String sql = "INSERT INTO nhat_ky(thoi_gian, nguoi_dung, hanh_dong, chi_tiet) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, log.getThoiGian());
            pstmt.setString(2, user);
            pstmt.setString(3, hanhDong);
            pstmt.setString(4, chiTiet);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (tabNhatKy != null) tabNhatKy.refreshLogTable();
    }
    
    public List<LogEntry> getDanhSachLog() { return danhSachLog; }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        header.setBackground(new Color(230, 240, 255));
        
        JLabel lblTitle = new JLabel("  HỆ THỐNG QUẢN LÝ NHÂN SỰ");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(Color.BLUE);
        header.add(lblTitle, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        lblThoiGianPhien = new JLabel("Phiên: 00:00:00  |  ");
        lblThoiGianPhien.setFont(new Font("Arial", Font.BOLD, 12));
        lblThoiGianPhien.setForeground(new Color(0, 102, 204));
        
        lblXinChao = new JLabel("Xin chào, ... | ");
        lblXinChao.setFont(new Font("Arial", Font.ITALIC, 12));
        
        btnTaoTaiKhoan = new JButton("Tạo TK mới");
        btnTaoTaiKhoan.setVisible(false); 
        btnTaoTaiKhoan.setBackground(new Color(0, 153, 76));
        btnTaoTaiKhoan.setForeground(Color.WHITE);
        btnTaoTaiKhoan.setFocusable(false);
        btnTaoTaiKhoan.addActionListener(e -> hienThiManHinhTaoTaiKhoan());
        
        JButton btnDoiMK = new JButton("Đổi mật khẩu");
        JButton btnDangXuat = new JButton("Đăng xuất");
        
        btnDoiMK.setFocusable(false);
        btnDangXuat.setFocusable(false);
        btnDangXuat.setBackground(new Color(255, 200, 200));

        btnDoiMK.addActionListener(e -> hienThiDoiMatKhau());
        btnDangXuat.addActionListener(e -> xuLyDangXuat());

        rightPanel.add(lblThoiGianPhien);
        rightPanel.add(lblXinChao);
        rightPanel.add(btnTaoTaiKhoan); 
        rightPanel.add(btnDoiMK);
        rightPanel.add(btnDangXuat);
        
        header.add(rightPanel, BorderLayout.EAST);
        return header; 
    }

    private void batDauDemGioLamViec() {
        startSessionTime = System.currentTimeMillis();
        if (sessionTimer != null) sessionTimer.stop();

        sessionTimer = new Timer(1000, e -> {
            long now = System.currentTimeMillis();
            long duration = now - startSessionTime;
            long seconds = (duration / 1000) % 60;
            long minutes = (duration / (1000 * 60)) % 60;
            long hours = (duration / (1000 * 60 * 60));
            lblThoiGianPhien.setText(String.format("Phiên: %02d:%02d:%02d  |  ", hours, minutes, seconds));
        });
        sessionTimer.start();
    }

    public void hienThiManHinhDangNhap() {
        JDialog loginDialog = new JDialog(this, "Đăng nhập Hệ thống", true);
        loginDialog.setSize(350, 200);
        loginDialog.setLayout(new GridBagLayout());
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loginDialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { System.exit(0); }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; loginDialog.add(new JLabel("Tài khoản:"), gbc);
        gbc.gridx = 1; JTextField txtUser = new JTextField(15); loginDialog.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 1; loginDialog.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; JPasswordField txtPass = new JPasswordField(15); loginDialog.add(txtPass, gbc);

        JPanel btnPanel = new JPanel();
        JButton btnLogin = new JButton("Đăng nhập");
        JButton btnExit = new JButton("Thoát");
        loginDialog.getRootPane().setDefaultButton(btnLogin);

        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            if (quanLyTaiKhoan.kiemTraDangNhap(user, pass)) {
                currentUser = user; 
                lblXinChao.setText("Xin chào, " + currentUser + " | "); 
                if (currentUser.equals("admin")) {
                    btnTaoTaiKhoan.setVisible(true);
                    if (tabbedPane.indexOfComponent(tabNhatKy) == -1) {
                        tabbedPane.addTab("Nhật ký hệ thống", null, tabNhatKy, "Xem lịch sử hoạt động");
                    }
                } else {
                    btnTaoTaiKhoan.setVisible(false);
                    int idx = tabbedPane.indexOfComponent(tabNhatKy);
                    if (idx != -1) tabbedPane.remove(idx);
                }
                ghiNhatKy("Đăng nhập", "Đăng nhập thành công");
                batDauDemGioLamViec(); 
                loginDialog.dispose(); 
            } else {
                JOptionPane.showMessageDialog(loginDialog, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnExit.addActionListener(e -> System.exit(0));
        btnPanel.add(btnLogin);
        btnPanel.add(btnExit);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        loginDialog.add(btnPanel, gbc);
        loginDialog.setVisible(true);
    }

    private void hienThiManHinhTaoTaiKhoan() {
        JDialog registerDialog = new JDialog(this, "Tạo tài khoản mới (Admin Only)", true);
        registerDialog.setSize(400, 250);
        registerDialog.setLayout(new GridBagLayout());
        registerDialog.setLocationRelativeTo(this);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNewUser = new JTextField(15);
        JPasswordField txtNewPass = new JPasswordField(15);
        JPasswordField txtConfirmPass = new JPasswordField(15);

        gbc.gridx = 0; gbc.gridy = 0; registerDialog.add(new JLabel("Tên đăng nhập mới:"), gbc);
        gbc.gridx = 1; registerDialog.add(txtNewUser, gbc);
        gbc.gridx = 0; gbc.gridy = 1; registerDialog.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; registerDialog.add(txtNewPass, gbc);
        gbc.gridx = 0; gbc.gridy = 2; registerDialog.add(new JLabel("Nhập lại Mật khẩu:"), gbc);
        gbc.gridx = 1; registerDialog.add(txtConfirmPass, gbc);

        JButton btnTaoTK = new JButton("Tạo tài khoản");
        btnTaoTK.addActionListener(e -> {
            String user = txtNewUser.getText().trim();
            String pass = new String(txtNewPass.getPassword());
            String confirm = new String(txtConfirmPass.getPassword());

            if (user.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(registerDialog, "Thiếu thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
            if (!pass.equals(confirm)) { JOptionPane.showMessageDialog(registerDialog, "Mật khẩu không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }

            boolean thanhCong = quanLyTaiKhoan.themTaiKhoan(user, pass);
            if (thanhCong) {
                JOptionPane.showMessageDialog(registerDialog, "Tạo tài khoản thành công!");
                ghiNhatKy("Tạo tài khoản", "Đã tạo user mới: " + user);
                registerDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(registerDialog, "Tên tài khoản đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; registerDialog.add(btnTaoTK, gbc);
        registerDialog.setVisible(true);
    }

    private void xuLyDangXuat() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ghiNhatKy("Đăng xuất", "Người dùng thoát phiên làm việc");
            if (sessionTimer != null) sessionTimer.stop();
            currentUser = ""; 
            btnTaoTaiKhoan.setVisible(false); 
            this.setVisible(false); 
            hienThiManHinhDangNhap(); 
            this.setVisible(true); 
        }
    }

    private void hienThiDoiMatKhau() {
        JDialog passDialog = new JDialog(this, "Đổi mật khẩu", true);
        passDialog.setSize(400, 250);
        passDialog.setLayout(new GridBagLayout());
        passDialog.setLocationRelativeTo(this);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;

        JPasswordField txtOldPass = new JPasswordField(15);
        JPasswordField txtNewPass = new JPasswordField(15);
        JPasswordField txtConfirmPass = new JPasswordField(15);

        gbc.gridx = 0; gbc.gridy = 0; passDialog.add(new JLabel("Mật khẩu cũ:"), gbc);
        gbc.gridx = 1; passDialog.add(txtOldPass, gbc);
        gbc.gridx = 0; gbc.gridy = 1; passDialog.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1; passDialog.add(txtNewPass, gbc);
        gbc.gridx = 0; gbc.gridy = 2; passDialog.add(new JLabel("Xác nhận MK mới:"), gbc);
        gbc.gridx = 1; passDialog.add(txtConfirmPass, gbc);

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.addActionListener(e -> {
            String oldP = new String(txtOldPass.getPassword());
            String newP = new String(txtNewPass.getPassword());
            String confirmP = new String(txtConfirmPass.getPassword());

            if (newP.isEmpty()) { JOptionPane.showMessageDialog(passDialog, "Mật khẩu trống!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
            if (!newP.equals(confirmP)) { JOptionPane.showMessageDialog(passDialog, "Không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }

            boolean ketQua = quanLyTaiKhoan.doiMatKhau(currentUser, oldP, newP);
            if (ketQua) {
                JOptionPane.showMessageDialog(passDialog, "Đổi mật khẩu thành công!");
                ghiNhatKy("Đổi mật khẩu", "User đã tự đổi mật khẩu");
                passDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(passDialog, "Mật khẩu cũ sai!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; passDialog.add(btnSave, gbc);
        passDialog.setVisible(true);
    }
    
    public void refreshAllTabs() {
        refreshTableNV();
        updatePhongBanComboBox();
        locNhanVienTheoPhongBan();
        updateDuAnComboBox();
        if (tabDuAn != null) tabDuAn.refreshTableDuAn(); 
        refreshLuongTable();
        refreshBaoCaoTab();
        if (tabNhatKy != null) tabNhatKy.refreshLogTable();
        if (tabNhatKy != null) tabNhatKy.refreshLogTable();
    }

    public void refreshBaoCaoTab() { if (tabBaoCao != null) tabBaoCao.refreshBaoCao(); }
    public void refreshLuongTable() { if (tabLuong != null) tabLuong.refreshLuongTable(); }
    public void locNhanVienTheoPhongBan() { if (tabPhongBan != null) tabPhongBan.locNhanVienTheoPhongBan(); }
    public void refreshTableNV() { if (tabNhanVien != null) tabNhanVien.refreshTableNV(); }
    public void updateDuAnComboBox() { if (tabDuAn != null) tabDuAn.updateDuAnComboBox(); }
    public void updatePhongBanComboBox() {
        if (tabNhanVien != null) tabNhanVien.updatePhongBanComboBox();
        if (tabPhongBan != null) tabPhongBan.updatePhongBanComboBox();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QuanLyNhanVienGUI app = new QuanLyNhanVienGUI();
            app.hienThiManHinhDangNhap(); 
            app.setVisible(true);
        });
    }
}
