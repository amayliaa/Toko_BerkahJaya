package tokohberkahjaya;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class FormCustomer extends JFrame {

    // COLOR PALETTE
    private static final Color C_PRIMARY = new Color(15, 52, 96),
        C_ACCENT  = new Color(0, 188, 212),  C_BG_DARK = new Color(10, 25, 47),
        C_BG      = new Color(241, 245, 249), C_WHITE   = Color.WHITE,
        C_BORDER  = new Color(226, 232, 240), C_MUTED   = new Color(100, 116, 139),
        C_DANGER  = new Color(239, 68, 68),   C_WARN    = new Color(245, 158, 11),
        C_FOCUS   = new Color(0, 188, 212),   C_GRAY    = new Color(100, 116, 139);

    private JTextField txtId, txtNama, txtAlamat, txtTelepon, txtCari;
    private JButton btnSimpan, btnUbah, btnHapus, btnReset, btnCari;
    private JTable tableCustomer;
    private DefaultTableModel model;
    private JLabel lblErrorNama, lblErrorTelepon;

    public FormCustomer(boolean isAdmin) {
        setTitle("TOKO BERKAH JAYA — Data Customer");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        tampilData();
        txtId.setText(generateIdCustomer());
        
        if (!isAdmin) {
            btnUbah.setVisible(false);
            btnHapus.setVisible(false);
            tableCustomer.setEnabled(false); // cegah klik baris untuk edit
        }
    }

    //VALIDASI

    private boolean validasiNama() {
        String v = txtNama.getText().trim();
        if (v.isEmpty())               { lblErrorNama.setText("Nama customer wajib diisi"); return false; }
        if (!v.matches("[a-zA-Z ]+")) { lblErrorNama.setText("Nama hanya boleh huruf dan spasi"); return false; }
        lblErrorNama.setText(" "); return true;
    }

    private boolean validasiTelepon() {
        String v = txtTelepon.getText().trim();
        if (v.isEmpty())                       { lblErrorTelepon.setText("Nomor telepon wajib diisi"); return false; }
        if (!v.matches("[0-9+\\-\\s]+")) { lblErrorTelepon.setText("Telepon hanya boleh angka, +, atau -"); return false; }
        lblErrorTelepon.setText(" "); return true;
    }

    //BUILD UI

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);

        // TOP BAR
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, C_BG_DARK, getWidth(), 0, C_PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 188, 212, 20));
                g2.fillOval(getWidth() - 120, -40, 180, 180);
                g2.dispose();
            }
        };
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(18, 28, 18, 28));
        topBar.setPreferredSize(new Dimension(0, 70));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topLeft.setOpaque(false);
        JLabel ico = new JLabel("\uD83D\uDC64  ");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22)); ico.setForeground(C_ACCENT);
        JLabel ttl = new JLabel("Data Customer");
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 20)); ttl.setForeground(C_WHITE);
        JLabel sub = new JLabel("   Kelola data pelanggan toko Anda");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12)); sub.setForeground(new Color(150, 190, 220));
        topLeft.add(ico); topLeft.add(ttl); topLeft.add(sub);

        JButton btnBack = buildTopButton("\u2190  Kembali");
        btnBack.addActionListener(e -> dispose());
        topBar.add(topLeft, BorderLayout.WEST);
        topBar.add(btnBack, BorderLayout.EAST);

        // BODY
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(18, 20, 18, 20));
        body.add(buildFormCard(),  BorderLayout.WEST);
        body.add(buildTableCard(), BorderLayout.CENTER);

        root.add(topBar, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        add(root);
    }

    //FORM CARD

    private JPanel buildFormCard() {
        JPanel card = buildCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setPreferredSize(new Dimension(300, 0));

        // fields
        txtId     = buildField(); txtId.setEditable(false); txtId.setForeground(C_MUTED);
        txtNama   = buildField();
        txtAlamat = buildField();
        txtTelepon= buildField();

        // error labels
        lblErrorNama    = errorLabel();
        lblErrorTelepon = errorLabel();

        // real-time validasi
        txtNama.addKeyListener(onKey(() -> validasiNama()));
        txtTelepon.addKeyListener(onKey(() -> validasiTelepon()));

        // max size untuk semua field
        for (JTextField f : new JTextField[]{txtId, txtNama, txtAlamat, txtTelepon})
            f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // title + accent line
        JLabel formTitle = new JLabel("Form Data Customer");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(C_PRIMARY);
        formTitle.setAlignmentX(LEFT_ALIGNMENT);

        card.add(formTitle);
        card.add(gap(8));
        card.add(buildAccentLine());
        card.add(gap(18));

        addRow(card, "ID Customer",    txtId,      null);
        addRow(card, "Nama Customer",  txtNama,    lblErrorNama);
        addRow(card, "Alamat",         txtAlamat,  null);
        addRow(card, "Telepon",        txtTelepon, lblErrorTelepon);
        card.add(gap(20));

        // tombol 2x2
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);
        btnGrid.setAlignmentX(LEFT_ALIGNMENT);
        btnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        btnSimpan = buildButton("Simpan", C_ACCENT);
        btnUbah   = buildButton("Ubah",   C_WARN);
        btnHapus  = buildButton("Hapus",  C_DANGER);
        btnReset  = buildButton("Reset",  C_GRAY);
        btnGrid.add(btnSimpan); btnGrid.add(btnUbah);
        btnGrid.add(btnHapus);  btnGrid.add(btnReset);
        card.add(btnGrid);
        card.add(Box.createVerticalGlue());

        // events
        btnSimpan.addActionListener(e -> simpanData());
        btnUbah.addActionListener(e -> ubahData());
        btnHapus.addActionListener(e -> hapusData());
        btnReset.addActionListener(e -> resetForm());
        txtTelepon.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) simpanData();
            }
        });

        return card;
    }

    //TABLE CARD

    private JPanel buildTableCard() {
        JPanel card = buildCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // search bar
        JPanel searchPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(txtCari != null && txtCari.hasFocus() ? C_ACCENT : C_BORDER);
                g2.setStroke(new BasicStroke(txtCari != null && txtCari.hasFocus() ? 1.8f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(220, 36));

        txtCari = new JTextField();
        txtCari.setOpaque(false);
        txtCari.setBorder(new EmptyBorder(4, 10, 4, 4));
        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCari.setForeground(new Color(30, 41, 59));

        btnCari = new JButton("\uD83D\uDD0D");
        btnCari.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btnCari.setOpaque(false); btnCari.setContentAreaFilled(false);
        btnCari.setBorderPainted(false); btnCari.setFocusPainted(false);
        btnCari.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCari.setPreferredSize(new Dimension(36, 36));
        searchPanel.add(txtCari, BorderLayout.CENTER);
        searchPanel.add(btnCari, BorderLayout.EAST);

        JLabel tblTitle = new JLabel("Daftar Customer");
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblTitle.setForeground(C_PRIMARY);

        JPanel tblHeader = new JPanel(new BorderLayout(12, 0));
        tblHeader.setOpaque(false);
        tblHeader.add(tblTitle,    BorderLayout.WEST);
        tblHeader.add(searchPanel, BorderLayout.EAST);

        // table
        model = new DefaultTableModel(
            new String[]{"ID Customer","Nama Customer","Alamat","Telepon"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tableCustomer = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) { c.setBackground(new Color(224,247,250)); c.setForeground(C_PRIMARY); }
                else { c.setBackground(row%2==0 ? C_WHITE : new Color(248,250,252)); c.setForeground(new Color(30,41,59)); }
                return c;
            }
        };
        tableCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableCustomer.setRowHeight(40);
        tableCustomer.setShowGrid(false);
        tableCustomer.setIntercellSpacing(new Dimension(0, 0));
        tableCustomer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCustomer.setFocusable(false);

        JTableHeader th = tableCustomer.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(241,245,249)); th.setForeground(C_PRIMARY);
        th.setBorder(BorderFactory.createMatteBorder(0,0,2,0, C_ACCENT));
        th.setPreferredSize(new Dimension(0, 42));
        ((DefaultTableCellRenderer) th.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer pad = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c) {
                super.getTableCellRendererComponent(t,v,s,f,r,c);
                setBorder(new EmptyBorder(0,14,0,14)); return this;
            }
        };
        int[] widths = {110, 180, 220, 130};
        for (int i = 0; i < 4; i++) {
            tableCustomer.getColumnModel().getColumn(i).setCellRenderer(pad);
            tableCustomer.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(tableCustomer);
        scroll.setBorder(BorderFactory.createMatteBorder(1,0,0,0, C_BORDER));
        scroll.getViewport().setBackground(C_WHITE);

        JLabel lblJumlah = new JLabel("Menampilkan 0 data");
        lblJumlah.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblJumlah.setForeground(C_MUTED);
        model.addTableModelListener(e -> lblJumlah.setText("Menampilkan " + model.getRowCount() + " data"));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8,2,0,2));
        footer.add(lblJumlah, BorderLayout.WEST);

        card.add(tblHeader, BorderLayout.NORTH);
        card.add(scroll,    BorderLayout.CENTER);
        card.add(footer,    BorderLayout.SOUTH);

        // events
        btnCari.addActionListener(e -> cariData());
        txtCari.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)  cariData();
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { txtCari.setText(""); tampilData(); }
            }
        });
        txtCari.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { searchPanel.repaint(); }
            @Override public void focusLost(FocusEvent e)   { searchPanel.repaint(); }
        });
        tableCustomer.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = tableCustomer.getSelectedRow();
                if (row >= 0) {
                    txtId.setText(model.getValueAt(row, 0).toString());
                    txtNama.setText(model.getValueAt(row, 1).toString());
                    txtAlamat.setText(model.getValueAt(row, 2).toString());
                    txtTelepon.setText(model.getValueAt(row, 3).toString());
                }
            }
        });

        return card;
    }

    //DATA OPERATIONS

    private void tampilData() {
        model.setRowCount(0);
        try {
            ResultSet rs = Koneksi.getKoneksi().createStatement()
                .executeQuery("SELECT * FROM customer ORDER BY id_customer ASC");
            while (rs.next())
                model.addRow(new Object[]{rs.getString("id_customer"), rs.getString("nama_customer"),
                    rs.getString("alamat"), rs.getString("telepon")});
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void cariData() {
        model.setRowCount(0);
        try {
            String k = "%" + txtCari.getText().trim() + "%";
            PreparedStatement pst = Koneksi.getKoneksi().prepareStatement(
                "SELECT * FROM customer WHERE id_customer LIKE ? OR nama_customer LIKE ? OR telepon LIKE ?");
            pst.setString(1, k); pst.setString(2, k); pst.setString(3, k);
            ResultSet rs = pst.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getString("id_customer"), rs.getString("nama_customer"),
                    rs.getString("alamat"), rs.getString("telepon")});
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void simpanData() {
        if (!validasiNama() || !validasiTelepon()) return;
        try {
            PreparedStatement pst = Koneksi.getKoneksi().prepareStatement("INSERT INTO customer VALUES(?,?,?,?)");
            pst.setString(1, txtId.getText().trim());     pst.setString(2, txtNama.getText().trim());
            pst.setString(3, txtAlamat.getText().trim()); pst.setString(4, txtTelepon.getText().trim());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data customer berhasil disimpan!");
            tampilData(); resetForm();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void ubahData() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih customer yang akan diubah!", "Peringatan", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!validasiNama() || !validasiTelepon()) return;
        try {
            PreparedStatement pst = Koneksi.getKoneksi().prepareStatement(
                "UPDATE customer SET nama_customer=?, alamat=?, telepon=? WHERE id_customer=?");
            pst.setString(1, txtNama.getText().trim()); pst.setString(2, txtAlamat.getText().trim());
            pst.setString(3, txtTelepon.getText().trim()); pst.setString(4, txtId.getText().trim());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data customer berhasil diubah!");
            tampilData(); resetForm();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void hapusData() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih customer yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE); return;
        }
        if (JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus customer: " + txtNama.getText() + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement pst = Koneksi.getKoneksi().prepareStatement("DELETE FROM customer WHERE id_customer=?");
            pst.setString(1, txtId.getText().trim());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data customer berhasil dihapus!");
            tampilData(); resetForm();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void resetForm() {
        txtId.setText(generateIdCustomer());
        txtNama.setText(""); txtAlamat.setText(""); txtTelepon.setText("");
        lblErrorNama.setText(" "); lblErrorTelepon.setText(" ");
        tableCustomer.clearSelection();
        txtNama.requestFocus();
    }

    private String generateIdCustomer() {
        try {
            ResultSet rs = Koneksi.getKoneksi().prepareStatement(
                "SELECT id_customer FROM customer ORDER BY id_customer DESC LIMIT 1").executeQuery();
            if (rs.next())
                return String.format("C%03d", Integer.parseInt(rs.getString("id_customer").replaceAll("[^0-9]","")) + 1);
        } catch (Exception ignored) {}
        return "C001";
    }

    //HELPER BUILDERS
    private void addRow(JPanel p, String label, JTextField field, JLabel errLbl) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(51, 65, 85));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbl); p.add(gap(4)); p.add(field);
        if (errLbl != null) { p.add(gap(2)); p.add(errLbl); }
        p.add(gap(12));
    }

    private JTextField buildField() {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? new Color(240,253,255) : new Color(248,250,252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? C_FOCUS : C_BORDER);
                g2.setStroke(new BasicStroke(hasFocus() ? 1.8f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        f.setOpaque(false); f.setBorder(new EmptyBorder(6,12,6,12));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(new Color(30,41,59));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e)   { f.repaint(); }
        });
        return f;
    }

    private JPanel buildCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(new Color(0,0,0,8)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
    }

    private JPanel buildAccentLine() {
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,C_ACCENT,getWidth(),0,new Color(0,188,212,0)));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        line.setOpaque(false);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        line.setAlignmentX(LEFT_ALIGNMENT);
        return line;
    }

    private JButton buildButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,getBackground().brighter(),0,getHeight(),getBackground()));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(255,255,255,35));
                g2.fillRoundRect(2,2,getWidth()-4,getHeight()/2,8,8);
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setBackground(color);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(color); btn.repaint(); }
        });
        return btn;
    }

    private JButton buildTopButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,20)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(0,188,212,80)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 36));
        return btn;
    }

    private JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Color.RED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    // Rigid area vertikal
    private Component gap(int h) { return Box.createRigidArea(new Dimension(0, h)); }

    // KeyAdapter yang memanggil Runnable saat keyReleased
    private KeyAdapter onKey(Runnable r) {
        return new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { r.run(); }
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormCustomer(true).setVisible(true));
    }
}