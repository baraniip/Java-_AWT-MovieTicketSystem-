import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import java.io.FileOutputStream;

// QR + PDF Libraries
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class UserDashboard extends Frame implements ActionListener {

    Label l1, l2, l3, titleLbl;
    Choice movieChoice, bookingChoice;
    java.awt.TextField nameField, seatsField; // 👈 Explicitly specify java.awt.TextField
    Button bookBtn, viewBtn, payBtn, cancelBtn, logoutBtn, ticketBtn;
    TextArea output;

    String lastMovie = "";
    String lastUser = "";
    int lastSeats = 0;
    double lastTotal = 0.0;
    boolean isPaid = false;

    public UserDashboard(String username) {
        setTitle("🎬 User Dashboard - Movie Booking");

        // ✅ Fullscreen
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setLayout(null);
        setBackground(new Color(240, 248, 255));

        // Fonts (use java.awt.Font explicitly)
        java.awt.Font titleFont = new java.awt.Font("Arial", java.awt.Font.BOLD, 26);
        java.awt.Font labelFont = new java.awt.Font("Arial", java.awt.Font.PLAIN, 16);

        // Title
        titleLbl = new Label("🎟️ Movie Ticket Booking Dashboard");
        titleLbl.setFont(titleFont);
        titleLbl.setForeground(new Color(0, 102, 204));
        titleLbl.setBounds(500, 60, 600, 40);
        add(titleLbl);

        // Labels
        l1 = new Label("Select Movie:");
        l1.setBounds(300, 150, 150, 30);
        l1.setFont(labelFont);
        add(l1);

        l2 = new Label("Your Name:");
        l2.setBounds(300, 200, 150, 30);
        l2.setFont(labelFont);
        add(l2);

        l3 = new Label("No. of Seats:");
        l3.setBounds(300, 250, 150, 30);
        l3.setFont(labelFont);
        add(l3);

        Label l4 = new Label("My Bookings:");
        l4.setBounds(300, 310, 150, 30);
        l4.setFont(labelFont);
        add(l4);

        // Fields & Choices
        movieChoice = new Choice();
        movieChoice.setBounds(480, 150, 400, 30);
        add(movieChoice);

        nameField = new java.awt.TextField(); // 👈 fully qualified
        nameField.setBounds(480, 200, 400, 30);
        nameField.setText(username);
        add(nameField);

        seatsField = new java.awt.TextField();
        seatsField.setBounds(480, 250, 400, 30);
        add(seatsField);

        bookingChoice = new Choice();
        bookingChoice.setBounds(480, 310, 400, 30);
        add(bookingChoice);

        // Buttons
        bookBtn = new Button("🎟️ Book Ticket");
        bookBtn.setBounds(300, 370, 140, 40);
        bookBtn.setBackground(new Color(102, 204, 255));
        bookBtn.addActionListener(this);
        add(bookBtn);

        payBtn = new Button("💳 Pay Now");
        payBtn.setBounds(460, 370, 140, 40);
        payBtn.setBackground(new Color(102, 255, 178));
        payBtn.addActionListener(this);
        add(payBtn);

        ticketBtn = new Button("🧾 Download Ticket");
        ticketBtn.setBounds(620, 370, 160, 40);
        ticketBtn.setBackground(new Color(255, 204, 102));
        ticketBtn.addActionListener(this);
        add(ticketBtn);

        cancelBtn = new Button("❌ Cancel Booking");
        cancelBtn.setBounds(800, 370, 160, 40);
        cancelBtn.setBackground(new Color(255, 102, 102));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(this);
        add(cancelBtn);

        viewBtn = new Button("📋 View Movies");
        viewBtn.setBounds(300, 430, 160, 40);
        viewBtn.setBackground(new Color(153, 102, 255));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.addActionListener(this);
        add(viewBtn);

        logoutBtn = new Button("🚪 Logout");
        logoutBtn.setBounds(480, 430, 120, 40);
        logoutBtn.setBackground(Color.GRAY);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(this);
        add(logoutBtn);

        // Output Area
        output = new TextArea();
        output.setBounds(300, 500, 800, 350);
        output.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 14));
        output.setEditable(false);
        add(output);

        loadMovies();
        loadUserBookings();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    // Load all movies
    private void loadMovies() {
        try (Connection con = DBConnection.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT title FROM movies");
            movieChoice.removeAll();
            while (rs.next()) {
                movieChoice.add(rs.getString("title"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading movies: " + e.getMessage());
        }
    }

    // Load user bookings
    private void loadUserBookings() {
        String username = nameField.getText();
        bookingChoice.removeAll();
        if (username.isEmpty()) return;
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT movie_title FROM bookings WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookingChoice.add(rs.getString("movie_title"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading bookings: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bookBtn) bookTicket();
        else if (e.getSource() == payBtn) simulatePayment();
        else if (e.getSource() == cancelBtn) cancelBooking();
        else if (e.getSource() == viewBtn) showMovies();
        else if (e.getSource() == ticketBtn) generateTicketPDF();
        else if (e.getSource() == logoutBtn) {
            dispose();
            new LoginFrame();
        }
    }

    private void bookTicket() {
        String movie = movieChoice.getSelectedItem();
        String name = nameField.getText();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Enter your name!");
            return;
        }
        int seats = Integer.parseInt(seatsField.getText());

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT price FROM movies WHERE title=?");
            ps.setString(1, movie);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double price = rs.getDouble("price");
                double total = price * seats;

                PreparedStatement insert = con.prepareStatement(
                        "INSERT INTO bookings (username, movie_title, seats, total_price, payment_status) VALUES (?, ?, ?, ?, 'Pending')");
                insert.setString(1, name);
                insert.setString(2, movie);
                insert.setInt(3, seats);
                insert.setDouble(4, total);
                insert.executeUpdate();

                lastMovie = movie;
                lastUser = name;
                lastSeats = seats;
                lastTotal = total;
                isPaid = false;

                JOptionPane.showMessageDialog(null, "Booking successful! Click Pay Now to simulate payment.");
                loadUserBookings();
            } else {
                JOptionPane.showMessageDialog(null, "Movie not found!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error booking ticket: " + ex.getMessage());
        }
    }

    private void simulatePayment() {
        String selectedBooking = bookingChoice.getSelectedItem();
        if (selectedBooking == null) {
            JOptionPane.showMessageDialog(null, "Select a booking first!");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
                "Confirm payment for " + selectedBooking + "?",
                "Payment Simulation", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE bookings SET payment_status='Paid' WHERE username=? AND movie_title=?");
                ps.setString(1, nameField.getText());
                ps.setString(2, selectedBooking);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "✅ Payment successful!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error updating payment: " + ex.getMessage());
            }
        }
    }

    private void cancelBooking() {
        String selectedBooking = bookingChoice.getSelectedItem();
        if (selectedBooking == null) {
            JOptionPane.showMessageDialog(null, "Select a booking to cancel!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM bookings WHERE username=? AND movie_title=?");
            ps.setString(1, nameField.getText());
            ps.setString(2, selectedBooking);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(null, "Booking cancelled successfully.");
                loadUserBookings();
            } else {
                JOptionPane.showMessageDialog(null, "No booking found.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error cancelling booking: " + ex.getMessage());
        }
    }

    private void showMovies() {
        try (Connection con = DBConnection.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM movies");

            StringBuilder sb = new StringBuilder();
            sb.append("ID | Title | Genre | Duration | Show Time | Price\n");
            sb.append("----------------------------------------------------\n");
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
            JOptionPane.showMessageDialog(null, "Error showing movies: " + ex.getMessage());
        }
    }

    private void generateTicketPDF() {
        String selectedBooking = bookingChoice.getSelectedItem();
        if (selectedBooking == null) {
            JOptionPane.showMessageDialog(null, "Select a booking first!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM bookings WHERE username=? AND movie_title=?");
            ps.setString(1, nameField.getText());
            ps.setString(2, selectedBooking);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getString("payment_status").equals("Paid")) {
                int seats = rs.getInt("seats");
                double total = rs.getDouble("total_price");

                String fileName = nameField.getText() + "_" + selectedBooking + "_Ticket.pdf";

                // QR code
                String qrData = "Movie: " + selectedBooking + "\nName: " + nameField.getText() +
                        "\nSeats: " + seats + "\nAmount: ₹" + total;
                BitMatrix matrix = new MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, 150, 150);
                java.nio.file.Path path = java.nio.file.FileSystems.getDefault().getPath("ticket_qr.png");
                MatrixToImageWriter.writeToPath(matrix, "PNG", path);

                Document doc = new Document();
                PdfWriter.getInstance(doc, new FileOutputStream(fileName));
                doc.open();

                com.itextpdf.text.Font titleFont =
                        new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 20, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Font normalFont =
                        new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.NORMAL);

                Paragraph title = new Paragraph("🎟️ Movie E-Ticket", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                doc.add(title);
                doc.add(new Paragraph("\n"));

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(80);
                table.setHorizontalAlignment(Element.ALIGN_CENTER);

                table.addCell("Movie Title");
                table.addCell(selectedBooking);
                table.addCell("Name");
                table.addCell(nameField.getText());
                table.addCell("Seats");
                table.addCell(String.valueOf(seats));
                table.addCell("Total Amount");
                table.addCell("₹" + total);
                table.addCell("Status");
                table.addCell("Paid");

                doc.add(table);
                doc.add(new Paragraph("\n"));

                com.itextpdf.text.Image qrImage =
                        com.itextpdf.text.Image.getInstance("ticket_qr.png");
                qrImage.setAlignment(Element.ALIGN_CENTER);
                doc.add(qrImage);

                doc.add(new Paragraph("\nThank you for booking!", normalFont));
                doc.close();

                JOptionPane.showMessageDialog(null, "🎫 E-Ticket PDF downloaded successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Payment not completed yet!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error generating ticket: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
