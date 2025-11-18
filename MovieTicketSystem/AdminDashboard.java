import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AdminDashboard extends Frame implements ActionListener {

    TextField t1, t2, t3, t4, t5;
    Button addBtn, viewBtn, backBtn;
    TextArea output;

    public AdminDashboard() {
        setTitle("🎬 Admin Dashboard - Movie Management");
        setSize(600, 500);
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 144, 255)); // Dodger Blue

        // Header
        Label header = new Label("🎬 Movie Management Dashboard", Label.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(Color.WHITE);
        add(header, BorderLayout.NORTH);

        // Form Panel
        Panel formPanel = new Panel(new GridBagLayout());
        formPanel.setBackground(new Color(173, 216, 230)); // Light Blue
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels and TextFields
        String[] labels = {"Title:", "Genre:", "Duration (mins):", "Show Time (yyyy-MM-dd HH:mm):", "Price:"};
        TextField[] fields = new TextField[5];

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            Label lbl = new Label(labels[i], Label.RIGHT);
            lbl.setFont(new Font("Arial", Font.BOLD, 14));
            formPanel.add(lbl, gbc);

            gbc.gridx = 1;
            TextField tf = new TextField();
            tf.setColumns(20); // smaller width
            fields[i] = tf;
            formPanel.add(tf, gbc);
        }

        t1 = fields[0];
        t2 = fields[1];
        t3 = fields[2];
        t4 = fields[3];
        t5 = fields[4];

        add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        addBtn = new Button("➕ Add Movie");
        addBtn.setBackground(new Color(60, 179, 113));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(this);
        btnPanel.add(addBtn);

        viewBtn = new Button("📋 View Movies");
        viewBtn.setBackground(new Color(255, 165, 0));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.addActionListener(this);
        btnPanel.add(viewBtn);

        backBtn = new Button("🚪 Logout");
        backBtn.setBackground(new Color(220, 20, 60));
        backBtn.setForeground(Color.WHITE);
        backBtn.addActionListener(this);
        btnPanel.add(backBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // Output Area
        output = new TextArea();
        output.setFont(new Font("Consolas", Font.PLAIN, 14));
        output.setEditable(false);
        output.setBackground(new Color(224, 255, 255));
        add(output, BorderLayout.EAST);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addBtn) addMovie();
        else if (e.getSource() == viewBtn) viewMovies();
        else if (e.getSource() == backBtn) {
            dispose();
            new LoginFrame();
        }
    }

    private void addMovie() {
        String title = t1.getText().trim();
        String genre = t2.getText().trim();
        String durStr = t3.getText().trim();
        String showTime = t4.getText().trim();
        String priceStr = t5.getText().trim();

        if (title.isEmpty() || genre.isEmpty() || durStr.isEmpty() || showTime.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields!");
            return;
        }

        int duration;
        double price;
        try {
            duration = Integer.parseInt(durStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Duration and Price must be numeric!");
            return;
        }

        // Validate show time format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setLenient(false);
        try {
            sdf.parse(showTime);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, "Show Time must be in format yyyy-MM-dd HH:mm");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO movies(title, genre, duration, show_time, price) VALUES (?, ?, ?, ?, ?)"
            );
            ps.setString(1, title);
            ps.setString(2, genre);
            ps.setInt(3, duration);
            ps.setString(4, showTime);
            ps.setDouble(5, price);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "🎬 Movie added successfully!");
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error adding movie: " + ex.getMessage());
        }
    }

    private void viewMovies() {
        try (Connection con = DBConnection.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM movies");

            StringBuilder sb = new StringBuilder();
            sb.append("ID | Title | Genre | Duration | Show Time | Price\n");
            sb.append("-------------------------------------------------\n");

            while (rs.next()) {
                sb.append(rs.getInt("movie_id")).append(" | ");
                sb.append(rs.getString("title")).append(" | ");
                sb.append(rs.getString("genre")).append(" | ");
                sb.append(rs.getInt("duration")).append(" | ");
                sb.append(rs.getString("show_time")).append(" | ");
                sb.append(rs.getDouble("price")).append("\n");
            }

            output.setText(sb.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error loading movies: " + ex.getMessage());
        }
    }

    private void clearFields() {
        t1.setText("");
        t2.setText("");
        t3.setText("");
        t4.setText("");
        t5.setText("");
    }

    public static void main(String[] args) {
        new AdminDashboard();
    }
}
