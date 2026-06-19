package tokohberkahjaya;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;

public class FormLogin extends JFrame {

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;

    //WARNA
    private static final Color C_PRIMARY     = new Color(15, 52, 96),
        C_ACCENT      = new Color(0, 188, 212),  C_ACCENT_DARK = new Color(0, 151, 167),
        C_BG_DARK     = new Color(10, 25, 47),   C_WHITE       = Color.WHITE,
        C_TEXT_MUTED  = new Color(100, 116, 139), C_BORDER     = new Color(226, 232, 240),
        C_FOCUS       = new Color(0, 188, 212);

    //KONSTRUKTOR
    public FormLogin() {
        setTitle("TOKO BERKAH JAYA — Login");
        setMinimumSize(new Dimension(700, 420));
        setSize(940, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildLeftPanel());
        root.add(buildRightPanel());
        add(root);

        // events
        btnLogin.addActionListener(e -> login());
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) login(); }
        });
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnLogin.setBackground(C_ACCENT_DARK); }
            @Override public void mouseExited(MouseEvent e)  { btnLogin.setBackground(C_ACCENT); }
        });
    }

    //PANEL KIRI (BRANDING)
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, C_BG_DARK, getWidth(), getHeight(), C_PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0,188,212,30)); g2.fillOval(-80,-80,300,300);
                g2.setColor(new Color(0,188,212,20)); g2.fillOval(getWidth()-160,getHeight()-160,260,260);
                g2.setColor(new Color(255,255,255,8)); g2.fillOval(40,getHeight()-180,200,200);
                g2.setColor(new Color(0,188,212,60)); g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(getWidth()/2-110,getHeight()/2-110,220,220);
                g2.setColor(new Color(0,188,212,25)); g2.setStroke(new BasicStroke(1f));
                g2.drawOval(getWidth()/2-140,getHeight()/2-140,280,280);
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());

        // icon
        JLabel ico = new JLabel("\uD83D\uDED2", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,188,212,40)); g2.fillOval(0,0,getWidth(),getHeight());
                g2.setColor(new Color(0,188,212,80)); g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(2,2,getWidth()-4,getHeight()-4); g2.dispose(); super.paintComponent(g);
            }
        };
        ico.setFont(new Font("Segoe UI Emoji",Font.PLAIN,42)); ico.setForeground(C_WHITE);
        ico.setPreferredSize(new Dimension(90,90)); ico.setMaximumSize(new Dimension(90,90));
        ico.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = lbl("TOKO BERKAH JAYA", new Font("Segoe UI",Font.BOLD,22), C_WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JPanel accentLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,new Color(0,188,212,0),getWidth()/2,0,C_ACCENT,true));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4); g2.dispose();
            }
        };
        accentLine.setOpaque(false); accentLine.setPreferredSize(new Dimension(120,3));
        accentLine.setMaximumSize(new Dimension(120,3)); accentLine.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub  = lbl("Sistem Penjualan Modern", new Font("Segoe UI",Font.PLAIN,13), new Color(180,210,240));
        JLabel tag  = lbl("Kelola toko Anda dengan mudah & efisien", new Font("Segoe UI",Font.ITALIC,11), new Color(120,160,200));
        sub.setAlignmentX(CENTER_ALIGNMENT); tag.setAlignmentX(CENTER_ALIGNMENT);

        JPanel content = new JPanel(); content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(ico);              content.add(Box.createRigidArea(new Dimension(0,16)));
        content.add(title);            content.add(Box.createRigidArea(new Dimension(0,10)));
        content.add(accentLine);       content.add(Box.createRigidArea(new Dimension(0,10)));
        content.add(sub);              content.add(Box.createRigidArea(new Dimension(0,8)));
        content.add(tag);
        panel.add(content);
        return panel;
    }

    //PANEL KANAN (FORM LOGIN
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(C_WHITE); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(0,188,212,15));
                for (int i=0;i<getWidth();i+=22) for (int j=0;j<getHeight();j+=22) g2.fillOval(i,j,3,3);
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());

        // form card
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(0,0,0,12)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20); g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32,36,36,36));
        card.setPreferredSize(new Dimension(360,390));

        JLabel loginTitle = lbl("Selamat Datang", new Font("Segoe UI",Font.BOLD,22), C_PRIMARY);
        JLabel loginSub   = lbl("Silakan masuk ke akun Anda", new Font("Segoe UI",Font.PLAIN,12), C_TEXT_MUTED);
        loginTitle.setAlignmentX(LEFT_ALIGNMENT); loginSub.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,C_ACCENT,getWidth(),0,new Color(0,188,212,0)));
                g2.fillRect(0,0,getWidth(),2); g2.dispose();
            }
        };
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,2)); sep.setAlignmentX(LEFT_ALIGNMENT);

        txtUsername = styledTextField();
        txtPassword = styledPasswordField();

        btnLogin = new JButton("MASUK") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,getBackground().brighter(),0,getHeight(),getBackground()));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(new Color(255,255,255,40)); g2.fillRoundRect(2,2,getWidth()-4,getHeight()/2,10,10);
                g2.setColor(C_WHITE); g2.setFont(getFont()); FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btnLogin.setBackground(C_ACCENT); btnLogin.setForeground(C_WHITE);
        btnLogin.setFont(new Font("Segoe UI",Font.BOLD,13));
        btnLogin.setFocusPainted(false); btnLogin.setBorderPainted(false); btnLogin.setContentAreaFilled(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(LEFT_ALIGNMENT); btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));

        JLabel footer = lbl("© 2026 Toko Berkah Jaya | Cahya Amaylia", new Font("Segoe UI",Font.PLAIN,10), new Color(180,180,180));
        footer.setAlignmentX(CENTER_ALIGNMENT);

        card.add(loginTitle);         card.add(Box.createRigidArea(new Dimension(0,4)));
        card.add(loginSub);           card.add(Box.createRigidArea(new Dimension(0,14)));
        card.add(sep);                card.add(Box.createRigidArea(new Dimension(0,20)));
        card.add(fieldLabel("Username")); card.add(Box.createRigidArea(new Dimension(0,6)));
        card.add(txtUsername);        card.add(Box.createRigidArea(new Dimension(0,16)));
        card.add(fieldLabel("Password")); card.add(Box.createRigidArea(new Dimension(0,6)));
        card.add(txtPassword);        card.add(Box.createRigidArea(new Dimension(0,24)));
        card.add(btnLogin);           card.add(Box.createRigidArea(new Dimension(0,16)));
        card.add(footer);

        panel.add(card);
        return panel;
    }

    //LOGIN
    private void login() {
        try {
            PreparedStatement pst = Koneksi.getKoneksi().prepareStatement(
                "SELECT * FROM user WHERE username=? AND password=?");
            pst.setString(1, txtUsername.getText());
            pst.setString(2, new String(txtPassword.getPassword()));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                SessionManager.setUser(rs.getString("username"), rs.getString("nama_lengkap"), rs.getString("level"));
                JOptionPane.showMessageDialog(null, "Login Berhasil!");
                new MenuUtama().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(null,"Username atau Password Salah","Login Gagal",JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(null,"Error : "+e.getMessage()); }
    }

    // HELPER BUILDERS

    private JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text); l.setFont(font); l.setForeground(color); return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = lbl(text, new Font("Segoe UI",Font.BOLD,12), new Color(51,65,85));
        l.setAlignmentX(LEFT_ALIGNMENT); return l;
    }

    //Logika paintComponent & paintBorder yang sama untuk TextField dan PasswordField
    private void paintRoundedField(JComponent f, Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(f.hasFocus() ? new Color(240,253,255) : new Color(248,250,252));
        g2.fillRoundRect(0,0,f.getWidth(),f.getHeight(),10,10); g2.dispose();
    }
    private void paintRoundedBorder(JComponent f, Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(f.hasFocus() ? C_FOCUS : C_BORDER);
        g2.setStroke(new BasicStroke(f.hasFocus() ? 1.8f : 1f));
        g2.drawRoundRect(0,0,f.getWidth()-1,f.getHeight()-1,10,10); g2.dispose();
    }

    private void styleField(JComponent f) {
        f.setOpaque(false); f.setBorder(new EmptyBorder(8,14,8,14));
        f.setFont(new Font("Segoe UI",Font.PLAIN,13));
        f.setPreferredSize(new Dimension(0,44));
        f.setAlignmentX(LEFT_ALIGNMENT);
        ((JComponent)f).setMaximumSize(new Dimension(Integer.MAX_VALUE,44));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e)   { f.repaint(); }
        });
    }

    private JTextField styledTextField() {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) { paintRoundedField(this,g); super.paintComponent(g); }
            @Override protected void paintBorder(Graphics g)    { paintRoundedBorder(this,g); }
        };
        f.setForeground(new Color(30,41,59)); styleField(f); return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) { paintRoundedField(this,g); super.paintComponent(g); }
            @Override protected void paintBorder(Graphics g)    { paintRoundedBorder(this,g); }
        };
        f.setForeground(new Color(30,41,59)); styleField(f); return f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormLogin().setVisible(true));
    }
}