import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class LoginFrame extends Frame implements ActionListener {

    Label titleLabel, userLabel, passLabel, roleLabel;
    TextField userField, passField;
    Choice roleChoice;
    Button loginBtn, exitBtn;

    public LoginFrame() {
        setTitle("🎟️ Movie Ticket System Login");
        setExtendedState(Frame.MAXIMIZED_BOTH); // Fullscreen
        setLayout(null);
        setBackground(new Color(25, 25, 112)); // Midnight Blue

        // Title
        titleLabel = new Label("🎬 Movie Ticket Booking System", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 100, 1920, 50);
        add(titleLabel);

        // Username
        userLabel = new Label("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userLabel.setForeground(Color.WHITE);
        userLabel.setBounds(700, 250, 120, 30);
        add(userLabel);

        userField = new TextField();
        userField.setBounds(850, 250, 250, 30);
        add(userField);

        // Password
        passLabel = new Label("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 18));
        passLabel.setForeground(Color.WHITE);
        passLabel.setBounds(700, 300, 120, 30);
        add(passLabel);

        passField = new TextField();
        passField.setEchoChar('*');
        passField.setBounds(850, 300, 250, 30);
        add(passField);

        // Role
        roleLabel = new Label("Login as:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setBounds(700, 350, 120, 30);
        add(roleLabel);

        roleChoice = new Choice();
        roleChoice.add("User");
        roleChoice.add("Admin");
        roleChoice.setBounds(850, 350, 250, 30);
        add(roleChoice);

        // Buttons
        loginBtn = new Button("Login");
        loginBtn.setBackground(new Color(60, 179, 113));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 16));
        loginBtn.setBounds(850, 420, 100, 40);
        loginBtn.addActionListener(this);
        add(loginBtn);

        exitBtn = new Button("Exit");
        exitBtn.setBackground(new Color(220, 20, 60));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFont(new Font("Arial", Font.BOLD, 16));
        exitBtn.setBounds(1000, 420, 100, 40);
        exitBtn.addActionListener(this);
        add(exitBtn);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            loginUser();
        } else if (e.getSource() == exitBtn) {
            System.exit(0);
        }
    }

    private void loginUser() {
        String username = userField.getText().trim();
        String password = passField.getText().trim();
        String role = roleChoice.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter both username and password!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps;
            ResultSet rs;

            if (role.equals("Admin")) {
                // ✅ Check in admin table
                ps = con.prepareStatement("SELECT * FROM admin WHERE username=? AND password=?");
                ps.setString(1, username);
                ps.setString(2, password);
                rs = ps.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(null, "✅ Welcome Admin: " + username);
                    dispose();
                    new AdminDashboard(); // open admin dashboard
                } else {
                    JOptionPane.showMessageDialog(null, "❌ Invalid admin credentials!");
                }

            } else {
                // ✅ Check in users table
                ps = con.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
                ps.setString(1, username);
                ps.setString(2, password);
                rs = ps.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(null, "✅ Welcome User: " + username);
                    dispose();
                    new UserDashboard(username); // open user dashboard
                } else {
                    JOptionPane.showMessageDialog(null, "❌ Invalid user credentials!");
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
