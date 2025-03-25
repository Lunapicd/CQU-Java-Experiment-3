import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentManagementSystem {
    // 数据库连接参数
    private static final String DB_URL = "jdbc:mysql://localhost:3306/studentdb";
    private static final String USER = "root";
    private static final String PASS = "password";

    // 登录界面
    private static class LoginFrame extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public LoginFrame() {
            setTitle("学生管理系统 - 登录");
            setSize(300, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel(new GridLayout(3, 2));
            panel.add(new JLabel("用户名:"));
            usernameField = new JTextField();
            panel.add(usernameField);

            panel.add(new JLabel("密码:"));
            passwordField = new JPasswordField();
            panel.add(passwordField);

            JButton loginButton = new JButton("登录");
            loginButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    login();
                }
            });
            panel.add(loginButton);

            add(panel);
        }

        private void login() {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            try {
                Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    // 登录成功，打开主界面
                    dispose();
                    new MainFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "用户名或密码错误");
                }

                rs.close();
                pstmt.close();
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "数据库连接错误");
            }
        }
    }

    // 主界面
    private static class MainFrame extends JFrame {
        private JTable studentTable;
        private DefaultTableModel tableModel;

        public MainFrame() {
            setTitle("学生管理系统");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // 表格模型
            String[] columnNames = {"学号", "姓名", "年龄", "班级"};
            tableModel = new DefaultTableModel(columnNames, 0);
            studentTable = new JTable(tableModel);

            // 按钮面板
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("添加");
            JButton deleteButton = new JButton("删除");
            JButton updateButton = new JButton("修改");
            JButton refreshButton = new JButton("刷新");

            addButton.addActionListener(e -> addStudent());
            deleteButton.addActionListener(e -> deleteStudent());
            updateButton.addActionListener(e -> updateStudent());
            refreshButton.addActionListener(e -> loadStudents());

            buttonPanel.add(addButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(refreshButton);

            // 布局
            setLayout(new BorderLayout());
            add(new JScrollPane(studentTable), BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            // 初始加载学生数据
            loadStudents();
        }

        private void loadStudents() {
            try {
                Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM students");

                // 清空表格
                tableModel.setRowCount(0);

                // 填充数据
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("class")
                    };
                    tableModel.addRow(row);
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "数据加载失败");
            }
        }

        private void addStudent() {
            // 添加学生对话框
            JTextField idField = new JTextField();
            JTextField nameField = new JTextField();
            JTextField ageField = new JTextField();
            JTextField classField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(4, 2));
            panel.add(new JLabel("学号:"));
            panel.add(idField);
            panel.add(new JLabel("姓名:"));
            panel.add(nameField);
            panel.add(new JLabel("年龄:"));
            panel.add(ageField);
            panel.add(new JLabel("班级:"));
            panel.add(classField);

            int result = JOptionPane.showConfirmDialog(this, panel, "添加学生", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                    PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO students (student_id, name, age, class) VALUES (?, ?, ?, ?)");
                    
                    pstmt.setString(1, idField.getText());
                    pstmt.setString(2, nameField.getText());
                    pstmt.setInt(3, Integer.parseInt(ageField.getText()));
                    pstmt.setString(4, classField.getText());

                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadStudents(); // 刷新表格
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "添加学生失败");
                }
            }
        }

        private void deleteStudent() {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要删除的学生");
                return;
            }

            String studentId = (String) tableModel.getValueAt(selectedRow, 0);

            try {
                Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM students WHERE student_id = ?");
                
                pstmt.setString(1, studentId);
                pstmt.executeUpdate();

                pstmt.close();
                conn.close();

                loadStudents(); // 刷新表格
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "删除学生失败");
            }
        }

        private void updateStudent() {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要修改的学生");
                return;
            }

            String studentId = (String) tableModel.getValueAt(selectedRow, 0);
            String currentName = (String) tableModel.getValueAt(selectedRow, 1);
            int currentAge = (int) tableModel.getValueAt(selectedRow, 2);
            String currentClass = (String) tableModel.getValueAt(selectedRow, 3);

            JTextField nameField = new JTextField(currentName);
            JTextField ageField = new JTextField(String.valueOf(currentAge));
            JTextField classField = new JTextField(currentClass);

            JPanel panel = new JPanel(new GridLayout(3, 2));
            panel.add(new JLabel("姓名:"));
            panel.add(nameField);
            panel.add(new JLabel("年龄:"));
            panel.add(ageField);
            panel.add(new JLabel("班级:"));
            panel.add(classField);

            int result = JOptionPane.showConfirmDialog(this, panel, "修改学生信息", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                    PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE students SET name = ?, age = ?, class = ? WHERE student_id = ?");
                    
                    pstmt.setString(1, nameField.getText());
                    pstmt.setInt(2, Integer.parseInt(ageField.getText()));
                    pstmt.setString(3, classField.getText());
                    pstmt.setString(4, studentId);

                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadStudents(); // 刷新表格
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "修改学生信息失败");
                }
            }
        }
    }

    // 数据库初始化方法
    private static void initDatabase() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();

            // 创建用户表
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(50) NOT NULL)");

            // 创建学生表
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                "student_id VARCHAR(20) PRIMARY KEY," +
                "name VARCHAR(50) NOT NULL," +
                "age INT," +
                "class VARCHAR(50))");

            // 插入默认用户
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT IGNORE INTO users (username, password) VALUES (?, ?)");
            pstmt.setString(1, "admin");
            pstmt.setString(2, "admin123");
            pstmt.executeUpdate();

            stmt.close();
            pstmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 加载JDBC驱动
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return;
        }

        // 初始化数据库
        initDatabase();

        // 设置Swing外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 启动登录界面
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}