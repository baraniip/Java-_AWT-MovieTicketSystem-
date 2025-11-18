import java.awt.*;
import java.awt.event.*;

public class BookingWindow extends Frame implements ActionListener {

    Label titleLbl, nameLbl, seatsLbl, totalLbl, thankLbl;
    Button okBtn;

    public BookingWindow(String movieTitle, String username, int seats, double total) {
        // Window setup
        setTitle("🎟️ Booking Confirmation");
        setSize(400, 320);
        setLayout(null);
        setBackground(new Color(240, 248, 255)); // light blue background

        // Movie title
        titleLbl = new Label("Movie: " + movieTitle);
        titleLbl.setBounds(60, 60, 300, 30);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 15));
        add(titleLbl);

        // Username
        nameLbl = new Label("Booked by: " + username);
        nameLbl.setBounds(60, 100, 300, 30);
        nameLbl.setFont(new Font("Arial", Font.PLAIN, 14));
        add(nameLbl);

        // Seats
        seatsLbl = new Label("Seats: " + seats);
        seatsLbl.setBounds(60, 140, 300, 30);
        seatsLbl.setFont(new Font("Arial", Font.PLAIN, 14));
        add(seatsLbl);

        // Total price
        totalLbl = new Label("Total Amount: ₹" + total);
        totalLbl.setBounds(60, 180, 300, 30);
        totalLbl.setFont(new Font("Arial", Font.PLAIN, 14));
        add(totalLbl);

        // Success message
        thankLbl = new Label("✅ Booking Successful!");
        thankLbl.setBounds(100, 220, 300, 30);
        thankLbl.setFont(new Font("Arial", Font.BOLD, 15));
        thankLbl.setForeground(new Color(0, 128, 0)); // green
        add(thankLbl);

        // OK button
        okBtn = new Button("OK");
        okBtn.setBounds(160, 260, 80, 30);
        okBtn.setBackground(new Color(0, 120, 215));
        okBtn.setForeground(Color.WHITE);
        okBtn.addActionListener(this);
        add(okBtn);

        // Close event
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });

        // Center window on screen
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okBtn) {
            dispose(); // close window
        }
    }

    // Test the window independently
    public static void main(String[] args) {
        new BookingWindow("Avengers: Endgame", "Barani", 2, 600.0);
    }
}
