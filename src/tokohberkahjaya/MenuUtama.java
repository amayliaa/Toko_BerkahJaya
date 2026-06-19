package tokohberkahjaya;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MenuUtama extends JFrame {

    private static final Color COLOR_PRIMARY    = new Color(15, 52, 96);
    private static final Color COLOR_ACCENT     = new Color(0, 188, 212);
    private static final Color COLOR_BG_DARK    = new Color(10, 25, 47);
    private static final Color COLOR_BG_CONTENT = new Color(241, 245, 249);
    private static final Color COLOR_TEXT_LIGHT = new Color(180, 210, 240);

    public MenuUtama() {
        setTitle("TOKO BERKAH JAYA — Menu Utama");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        // ROOT PANEL — background gradient gelap
        JPanel rootPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background gradient
                GradientPaint bg = new GradientPaint(0, 0, COLOR_BG_DARK, getWidth(), getHeight(), COLOR_PRIMARY);
                g2.setPaint(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Dekorasi lingkaran transparan
                g2.setColor(new Color(0, 188, 212, 15));
                g2.fillOval(-100, -100, 400, 400);
                g2.setColor(new Color(0, 188, 212, 10));
                g2.fillOval(getWidth() - 250, getHeight() - 200, 500, 400);
                g2.setColor(new Color(0, 188, 212, 8));
                g2.fillOval(getWidth() / 2 - 150, getHeight() - 100, 300, 250);

                // Titik-titik dekoratif
                g2.setColor(new Color(255, 255, 255, 8));
                for (int i = 0; i < getWidth(); i += 40)
                    for (int j = 0; j < getHeight(); j += 40)
                        g2.fillOval(i, j, 2, 2);

                g2.dispose();
            }
        };
        rootPanel.setOpaque(false);

        // HEADER — logo + info admin + tombol logout
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 188, 212, 40));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 36, 18, 36));

        // Kiri: logo + nama toko
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoArea.setOpaque(false);

        JLabel iconLabel = new JLabel("\uD83D\uDED2");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        iconLabel.setForeground(COLOR_ACCENT);

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));

        JLabel tokoName = new JLabel("TOKO BERKAH JAYA");
        tokoName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tokoName.setForeground(Color.WHITE);

        JLabel tokoSub = new JLabel("Sistem Penjualan Modern");
        tokoSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tokoSub.setForeground(COLOR_TEXT_LIGHT);

        titleArea.add(tokoName);
        titleArea.add(tokoSub);

        logoArea.add(iconLabel);
        logoArea.add(titleArea);

        // Kanan: info admin + logout
        JPanel rightArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightArea.setOpaque(false);

        JLabel adminLabel = new JLabel(SessionManager.getLevel() + "  •  " + SessionManager.getNamaLengkap());
        adminLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        adminLabel.setForeground(COLOR_TEXT_LIGHT);

        JButton btnLogout = createLogoutButton();
        btnLogout.addActionListener(e -> { SessionManager.clear(); new FormLogin().setVisible(true); dispose(); });

        rightArea.add(adminLabel);
        rightArea.add(btnLogout);

        header.add(logoArea, BorderLayout.WEST);
        header.add(rightArea, BorderLayout.EAST);

        // CENTER — judul selamat datang + grid kartu
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel centerContent = new JPanel();
        centerContent.setOpaque(false);
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Selamat Datang, " + SessionManager.getNamaLengkap() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Pilih menu di bawah untuk memulai");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(COLOR_TEXT_LIGHT);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Garis aksen kecil di bawah judul
        JPanel accentLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        accentLine.setOpaque(false);
        accentLine.setPreferredSize(new Dimension(60, 3));
        accentLine.setMaximumSize(new Dimension(60, 3));
        accentLine.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Grid 2×2 kartu menu
        JPanel cardGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        cardGrid.setOpaque(false);
        cardGrid.setMaximumSize(new Dimension(700, 380));
        cardGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardGrid.add(createMenuCard("\uD83D\uDCB0", "Transaksi Penjualan",
                "Catat & kelola transaksi", new Color(0, 188, 212),
                e -> new FormTransaksi().setVisible(true)));

        cardGrid.add(createMenuCard("\uD83D\uDCC4", "Laporan Penjualan",
                "Lihat rekap & laporan", new Color(72, 199, 142),
                e -> new FormLaporan().setVisible(true)));
        
        final boolean adminMode = SessionManager.isAdmin();
            cardGrid.add(createMenuCard("\uD83D\uDC64",
                    adminMode ? "Data Customer" : "Data Customer",
                    adminMode ? "Kelola data pelanggan" : "Tambah data pelanggan",
                    new Color(255, 167, 38),
                    e -> new FormCustomer(adminMode).setVisible(true)));
        
            cardGrid.add(createMenuCard(
                "\uD83D\uDCE6",
                adminMode ? "Data Barang" : "Lihat Data Barang",
                adminMode ? "Kelola stok & barang" : "Lihat stok & barang",
                adminMode ? new Color(236, 100, 120) : new Color(150, 150, 160),
                e -> new FormDataBarang(adminMode).setVisible(true)));

        centerContent.add(welcomeLabel);
        centerContent.add(Box.createRigidArea(new Dimension(0, 6)));
        centerContent.add(accentLine);
        centerContent.add(Box.createRigidArea(new Dimension(0, 6)));
        centerContent.add(subLabel);
        centerContent.add(Box.createRigidArea(new Dimension(0, 28)));
        centerContent.add(cardGrid);

        centerWrapper.add(centerContent);

        // FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 188, 212, 30));
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 14, 0));

        JLabel footerText = new JLabel("© 2025 Toko Berkah Jaya | Cahya Amaylia. All rights reserved.");
        footerText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerText.setForeground(new Color(100, 130, 160));
        footer.add(footerText);

        rootPanel.add(header, BorderLayout.NORTH);
        rootPanel.add(centerWrapper, BorderLayout.CENTER);
        rootPanel.add(footer, BorderLayout.SOUTH);

        add(rootPanel);
    }

    // KARTU MENU BESAR
    private JPanel createMenuCard(String emoji, String title, String subtitle,
                                  Color accentColor, ActionListener action) {
        JPanel card = new JPanel() {
            private boolean hover = false;

            {
                setOpaque(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) { action.actionPerformed(null); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background kartu
                Color bgCard = hover
                        ? new Color(255, 255, 255, 28)
                        : new Color(255, 255, 255, 15);
                g2.setColor(bgCard);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                // Border kartu
                g2.setColor(hover
                        ? new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 160)
                        : new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 18, 18);

                // Bar warna aksen di atas
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 5, 8, 8);
                g2.fillRect(0, 2, getWidth(), 5); // tutup bagian bawah agar rata

                // Lingkaran ikon background
                int cx = getWidth() / 2;
                Color iconBg = new Color(accentColor.getRed(), accentColor.getGreen(),
                        accentColor.getBlue(), hover ? 40 : 25);
                g2.setColor(iconBg);
                g2.fillOval(cx - 36, 24, 72, 72);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(26, 18, 22, 18));
        card.setPreferredSize(new Dimension(320, 185));

        JLabel emojiLabel = new JLabel(emoji, SwingConstants.CENTER);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        emojiLabel.setForeground(accentColor);
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(180, 210, 240));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createRigidArea(new Dimension(0, 28))); // ruang untuk lingkaran ikon
        card.add(emojiLabel);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(subLabel);

        return card;
    }

    // TOMBOL LOGOUT
    private JButton createLogoutButton() {
        JButton btn = new JButton("Logout") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(220, 60, 60, 180)
                        : new Color(180, 40, 40, 120);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(255, 120, 120, 120));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(255, 160, 160));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 34));
        btn.getModel().addChangeListener(e -> btn.repaint());
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuUtama().setVisible(true));
    }
}