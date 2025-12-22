import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuanLyNhanVienGUI extends JFrame {

    // Danh sách dữ liệu (Cache trên RAM)
    List<NhanVien> danhSachNV;
    List<PhongBan> danhSachPB;
    List<DuAn> danhSachDuAn;
    private List<LogEntry> danhSachLog;

    // Các thành phần giao diện
    private JTabbedPane tabbedPane;
    
    // --- CÁC TAB CHỨC NĂNG ---
    private TabDashboard tabDashboard; 
    private TabNhanVien tabNhanVien;
    private TabPhongBan tabPhongBan;
    private TabDuAn tabDuAn;
    private TabHieuSuat tabHieuSuat;
    private TabLichLamViec tabLichLamViec; // [MỚI] Thêm tab Lịch làm việc
    private TabLuong tabLuong;
    private TabBaoCao tabBaoCao;
    private TabNhatKy tabNhatKy;
    
    // --- CÁC TAB MỚI (ERP - Giai đoạn 3) ---
    private TabTuyenDung tabTuyenDung;
    private TabDaoTao tabDaoTao;
    private TabTaiSan tabTaiSan;
    
    // --- CÁC TAB MỚI (Admin & Security) ---
    private TabHeThong tabHeThong;
    private TabEmail tabEmail; // [MỚI]

    // Các tiện ích
    NumberFormat currencyFormatter;
    private QuanLyTaiKhoan quanLyTaiKhoan; 
    private String currentUser = ""; 
    
    // Header Components
    private JButton btnTaoTaiKhoan; 
    private JLabel lblXinChao; 
    private JLabel lblThoiGianPhien; 
    private Timer sessionTimer;      
    private long startSessionTime;   

    public QuanLyNhanVienGUI() {
        // 1. Khởi tạo Database
        DatabaseHandler.createNewDatabase();
        
        // 2. Thiết lập cửa sổ chính
        setTitle("Hệ thống Quản trị Doanh nghiệp Tổng thể (ERP) - Full Version");
        setSize(1500,800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        // 3. Khởi tạo tiện ích
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
        quanLyTaiKhoan = new QuanLyTaiKhoan();
        
        // 4. Khởi tạo danh sách
        danhSachNV = new ArrayList<>();
        danhSachPB = new ArrayList<>();
        danhSachDuAn = new ArrayList<>();
        danhSachLog = new ArrayList<>();
        
        // 5. Nạp dữ liệu
        loadDataFromDB();
        
        ghiNhatKy("Khởi động", "Ứng dụng đã được bật lên");

        // 6. Tạo giao diện
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        
        // Init Tabs
        tabDashboard = new TabDashboard(this); 
        tabNhanVien = new TabNhanVien(this);
        tabPhongBan = new TabPhongBan(this);
        tabDuAn = new TabDuAn(this);
        tabLuong = new TabLuong(this);
        tabHieuSuat = new TabHieuSuat(this);
        tabLichLamViec = new TabLichLamViec(this); // [MỚI] Khởi tạo tab Lịch
        tabTuyenDung = new TabTuyenDung(this);
        tabDaoTao = new TabDaoTao(this);
        tabTaiSan = new TabTaiSan(this);
        tabBaoCao = new TabBaoCao(this);
        tabNhatKy = new TabNhatKy(this);
        tabHeThong = new TabHeThong(this);
        tabEmail = new TabEmail(this); // [MỚI]

        // Mặc định hiển thị Dashboard trước.
        tabbedPane.addTab("Dashboard", new ImageIcon(), tabDashboard, "Tổng quan hệ thống");

        add(tabbedPane, BorderLayout.CENTER);
        
        refreshAllTabs();
    }
    
    public String getCurrentUser() {
        return currentUser;
    }

    // Hàm phân quyền hiển thị Tab
    private void setupTabsByRole(String role) {
        tabbedPane.removeAll();
        
        // Ai cũng thấy Dashboard
        tabbedPane.addTab("Dashboard", new ImageIcon(), tabDashboard, "Tổng quan");
        
        if (role.equals("admin")) {
            tabbedPane.addTab("Quản lý Nhân sự", null, tabNhanVien, "Quản lý hồ sơ nhân viên");
            tabbedPane.addTab("Phòng ban", null, tabPhongBan, "Xem nhân viên theo phòng ban");
            tabbedPane.addTab("Lịch Làm Việc", null, tabLichLamViec, "Xếp lịch & Phân ca"); // [MỚI]
            tabbedPane.addTab("Chấm công & Nghỉ phép", null, tabHieuSuat, "Quản lý thời gian làm việc & Vi phạm");
            tabbedPane.addTab("Quản lý Lương", null, tabLuong, "Tính lương & Xuất phiếu lương");
            tabbedPane.addTab("Quản lý Dự án", null, tabDuAn, "Phân công dự án");
            tabbedPane.addTab("Tuyển dụng", null, tabTuyenDung, "Quản lý Tin tuyển dụng & Ứng viên");
            tabbedPane.addTab("Đào tạo", null, tabDaoTao, "Quản lý Khóa học nội bộ");
            tabbedPane.addTab("Tài sản", null, tabTaiSan, "Quản lý cấp phát Tài sản/Thiết bị");
            tabbedPane.addTab("Gửi Email", null, tabEmail, "Gửi thông báo nội bộ"); // [MỚI]
            tabbedPane.addTab("Báo cáo & Thống kê", null, tabBaoCao, "Xem biểu đồ thưởng phạt");
            tabbedPane.addTab("Nhật ký hệ thống", null, tabNhatKy, "Log admin");
            tabbedPane.addTab("Hệ thống & Bảo mật", null, tabHeThong, "Backup, Restore & Đổi mật khẩu");
        } 
        else if (role.equals("hr")) {
            tabbedPane.addTab("Quản lý Nhân sự", null, tabNhanVien, "");
            tabbedPane.addTab("Tuyển dụng", null, tabTuyenDung, "");
            tabbedPane.addTab("Đào tạo", null, tabDaoTao, "");
            tabbedPane.addTab("Lịch Làm Việc", null, tabLichLamViec, "Xếp lịch & Phân ca"); // [MỚI]
            tabbedPane.addTab("Chấm công", null, tabHieuSuat, "");
            tabbedPane.addTab("Gửi Email", null, tabEmail, ""); // [MỚI]
            tabbedPane.addTab("Phòng ban", null, tabPhongBan, "");
            tabbedPane.addTab("Hệ thống", null, tabHeThong, "Đổi mật khẩu");
        } 
        else if (role.equals("accountant")) {
            tabbedPane.addTab("Quản lý Lương", null, tabLuong, "");
            tabbedPane.addTab("Tài sản", null, tabTaiSan, "");
            tabbedPane.addTab("Báo cáo", null, tabBaoCao, "");
            tabbedPane.addTab("Chấm công", null, tabHieuSuat, "Xem công để tính lương");
            tabbedPane.addTab("Hệ thống", null, tabHeThong, "Đổi mật khẩu");
        }
        else {
            // User thường
            tabbedPane.addTab("Lịch Làm Việc", null, tabLichLamViec, "Xem lịch làm việc cá nhân"); // [MỚI] - Cho phép nhân viên xem lịch
            tabbedPane.addTab("Thông tin cá nhân", null, new JPanel(), "Đang cập nhật...");
            tabbedPane.addTab("Hệ thống", null, tabHeThong, "Đổi mật khẩu");
        }
        refreshAllTabs();
    }

    // ========================================================================
    //                              PHẦN XỬ LÝ DỮ LIỆU (DATABASE)
    // ========================================================================
    
    private void loadDataFromDB() {
        try (Connection conn = DatabaseHandler.connect();
             Statement stmt = conn.createStatement()) {
            
            // --- 1. Load Phòng ban ---
            ResultSet rsCheckPB = stmt.executeQuery("SELECT COUNT(*) FROM phong_ban");
            rsCheckPB.next();
            if (rsCheckPB.getInt(1) == 0) {
                stmt.execute("INSERT INTO phong_ban VALUES ('KT', 'Kỹ thuật')");
                stmt.execute("INSERT INTO phong_ban VALUES ('KD', 'Kinh doanh')");
                stmt.execute("INSERT INTO phong_ban VALUES ('NS', 'Nhân sự')");
            }
            rsCheckPB.close();
            ResultSet rsPB = stmt.executeQuery("SELECT * FROM phong_ban");
            while (rsPB.next()) {
                danhSachPB.add(new PhongBan(rsPB.getString("ma_pb"), rsPB.getString("ten_pb")));
            }
            rsPB.close();

            // --- 2. Load Dự án ---
            ResultSet rsCheckDA = stmt.executeQuery("SELECT COUNT(*) FROM du_an");
            rsCheckDA.next();
            if (rsCheckDA.getInt(1) == 0) {
                stmt.execute("INSERT INTO du_an (ma_da, ten_da, do_phuc_tap) VALUES ('DA01', 'Website TMĐT', 3)");
                stmt.execute("INSERT INTO du_an (ma_da, ten_da, do_phuc_tap) VALUES ('DA02', 'Hệ thống CRM nội bộ', 2)");
            }
            ResultSet rsDA = stmt.executeQuery("SELECT * FROM du_an");
            while (rsDA.next()) {
                danhSachDuAn.add(new DuAn(rsDA.getString("ma_da"), rsDA.getString("ten_da"), rsDA.getInt("do_phuc_tap")));
            }

            // --- 3. Load Nhân viên ---
            ResultSet rsCheckNV = stmt.executeQuery("SELECT COUNT(*) FROM nhan_vien");
            rsCheckNV.next();
            int soLuongNV = rsCheckNV.getInt(1);
            rsCheckNV.close(); // <--- QUAN TRỌNG: Phải đóng cái này lại trước khi thêm mới
            if (soLuongNV == 0) {
                //System.out.println("⚡ Database trống, đang khởi tạo nhân viên mẫu...");
                // Gọi hàm bên DataSeeder và truyền kết nối hiện tại (conn) vào
                DataSeeder.themNhanVienMau(conn);
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

            // --- 4. Load Phân công Dự án ---
            ResultSet rsPC = stmt.executeQuery("SELECT * FROM phan_cong");
            while (rsPC.next()) {
                String maDA = rsPC.getString("ma_da");
                String maNV = rsPC.getString("ma_nv");

                DuAn da = null;
                for (DuAn d : danhSachDuAn) {
                    if (d.getMaDuAn().equals(maDA)) { da = d; break; }
                }
                
                NhanVien nv = null;
                for (NhanVien n : danhSachNV) {
                    if (n.getMaNhanVien().equals(maNV)) { nv = n; break; }
                }

                if (da != null && nv != null) {
                    da.addThanhVien(nv);
                }
            }

            // --- 5. Load Nhật ký (Log) ---
            ResultSet rsLog = stmt.executeQuery("SELECT * FROM nhat_ky ORDER BY id DESC LIMIT 50");
            while (rsLog.next()) {
                 danhSachLog.add(new LogEntry(
                     rsLog.getString("thoi_gian"),
                     rsLog.getString("nguoi_dung"), 
                     rsLog.getString("hanh_dong"), 
                     rsLog.getString("chi_tiet")
                 ));
            }

            System.out.println("✅ Đã nạp dữ liệu thành công lên RAM.");

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

    // ========================================================================
    //                              PHẦN GIAO DIỆN (UI)
    // ========================================================================

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        header.setBackground(new Color(230, 240, 255));
        
        JLabel lblTitle = new JLabel("  HỆ THỐNG QUẢN TRỊ DOANH NGHIỆP (ERP)");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(new Color(0, 51, 153));
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
        
        btnTaoTaiKhoan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hienThiManHinhTaoTaiKhoan();
            }
        });
        
        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.setFocusable(false);
        btnDangXuat.setBackground(new Color(255, 200, 200));

        btnDangXuat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xuLyDangXuat();
            }
        });

        rightPanel.add(lblThoiGianPhien);
        rightPanel.add(lblXinChao);
        rightPanel.add(btnTaoTaiKhoan); 
        rightPanel.add(btnDangXuat);
        
        header.add(rightPanel, BorderLayout.EAST);
        return header; 
    }

    private void batDauDemGioLamViec() {
        startSessionTime = System.currentTimeMillis();
        if (sessionTimer != null) sessionTimer.stop();

        sessionTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long now = System.currentTimeMillis();
                long duration = now - startSessionTime;
                long seconds = (duration / 1000) % 60;
                long minutes = (duration / (1000 * 60)) % 60;
                long hours = (duration / (1000 * 60 * 60));
                lblThoiGianPhien.setText(String.format("Phiên: %02d:%02d:%02d  |  ", hours, minutes, seconds));
            }
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
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0); 
            }
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

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = txtUser.getText();
                String pass = new String(txtPass.getPassword());

                // Lấy Role thay vì chỉ True/False
                String role = quanLyTaiKhoan.dangNhap(user, pass);

                if (role != null) {
                    currentUser = user; 
                    lblXinChao.setText("Xin chào, " + currentUser + " (" + role + ") | "); 
                    
                    if (role.equals("admin")) {
                        btnTaoTaiKhoan.setVisible(true);
                    } else {
                        btnTaoTaiKhoan.setVisible(false);
                    }
                    
                    // Gọi hàm setup lại các Tab theo quyền
                    setupTabsByRole(role);
                    
                    ghiNhatKy("Đăng nhập", "Role: " + role);
                    batDauDemGioLamViec(); 
                    loginDialog.dispose(); 
                } else {
                    JOptionPane.showMessageDialog(loginDialog, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
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
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        
        JTextField txtNewUser = new JTextField();
        JPasswordField txtNewPass = new JPasswordField();
        
        // ComboBox chọn Role
        String[] roles = {"user", "admin", "hr", "accountant"};
        JComboBox<String> cmbRole = new JComboBox<>(roles);
        
        panel.add(new JLabel("Tên đăng nhập mới:"));
        panel.add(txtNewUser);
        
        panel.add(new JLabel("Mật khẩu:"));
        panel.add(txtNewPass);
        
        panel.add(new JLabel("Vai trò (Role):"));
        panel.add(cmbRole);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tạo Tài khoản & Phân quyền", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newUser = txtNewUser.getText().trim();
            String newPass = new String(txtNewPass.getPassword()).trim();
            String role = (String) cmbRole.getSelectedItem();

            if (newUser.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            // Gọi hàm thêm tài khoản có tham số Role
            boolean thanhCong = quanLyTaiKhoan.themTaiKhoan(newUser, newPass, role);
            
            if (thanhCong) {
                JOptionPane.showMessageDialog(this, "Tạo tài khoản thành công!\nUser: " + newUser + "\nRole: " + role);
                ghiNhatKy("Tạo tài khoản", "Đã tạo user: " + newUser + " (Quyền: " + role + ")");
            } else {
                JOptionPane.showMessageDialog(this, "Tên tài khoản đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
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
    
    public void refreshAllTabs() {
        if (tabDashboard != null) tabDashboard.refreshDashboard(); 
        if (tabNhanVien != null) tabNhanVien.refreshTableNV();
        if (tabNhanVien != null) tabNhanVien.updatePhongBanComboBox();
        
        if (tabPhongBan != null) {
            tabPhongBan.updatePhongBanComboBox();
            tabPhongBan.locNhanVienTheoPhongBan();
        }
        
        if (tabDuAn != null) {
            tabDuAn.refreshTableDuAn(); 
            tabDuAn.updateDuAnComboBox();
        }
        
        if (tabLuong != null) tabLuong.refreshLuongTable();
        if (tabBaoCao != null) tabBaoCao.refreshBaoCao();
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

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                QuanLyNhanVienGUI app = new QuanLyNhanVienGUI();
                app.hienThiManHinhDangNhap(); 
                app.setVisible(true);
            }
        });
    }
}