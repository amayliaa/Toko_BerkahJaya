package tokohberkahjaya;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class FormLaporan extends JFrame {

    //WARNA
    private static final Color C_PRIMARY  = new Color(15, 52, 96),
        C_ACCENT  = new Color(0, 188, 212),  C_BG_DARK = new Color(10, 25, 47),
        C_BG      = new Color(241, 245, 249), C_WHITE   = Color.WHITE,
        C_BORDER  = new Color(226, 232, 240), C_MUTED   = new Color(100, 116, 139),
        C_SUCCESS = new Color(16, 185, 129),  C_ROW_ALT = new Color(248, 250, 252);

    private static final DecimalFormat DF = new DecimalFormat("#,##0");

    //KOMPONEN
    private JTable             tblLaporan;
    private DefaultTableModel  model;
    private JTextField         txtCari;
    private JLabel             lblTotalTransaksi, lblTotalPendapatan, lblTotalItem;
    private JComboBox<String>  cmbFilter;

    //SQL TEMPLATE
    private static final String SQL_BASE =
        "SELECT p.no_faktur, p.tgl_transaksi, p.id_customer, c.nama_customer, " +
        "b.nama_barang, d.harga_satuan, d.jumlah_beli, d.subtotal, p.total_bayar " +
        "FROM penjualan p " +
        "JOIN detail_penjualan d ON p.id_jual=d.id_jual " +
        "LEFT JOIN customer c    ON p.id_customer=c.id_customer " +
        "LEFT JOIN barang b      ON d.id_barang=b.id_barang ";

    //KONSTRUKTOR
    public FormLaporan() {
        setTitle("TOKO BERKAH JAYA — Laporan Penjualan");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        tampilData();
    }

    //BUILD UI
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        root.add(buildTopBar(),  BorderLayout.NORTH);
        root.add(buildBody(),    BorderLayout.CENTER);
        add(root);
    }

    //TOP BAR
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
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
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(18, 28, 18, 28));
        bar.setPreferredSize(new Dimension(0, 70));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel ico = new JLabel("\uD83D\uDCC4  "); ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22)); ico.setForeground(C_ACCENT);
        JLabel ttl = new JLabel("Laporan Penjualan"); ttl.setFont(new Font("Segoe UI", Font.BOLD, 20)); ttl.setForeground(C_WHITE);
        JLabel sub = new JLabel("   Rekap seluruh transaksi penjualan"); sub.setFont(new Font("Segoe UI", Font.PLAIN, 12)); sub.setForeground(new Color(150, 190, 220));
        left.add(ico); left.add(ttl); left.add(sub);

        JButton back = buildTopBtn("\u2190  Kembali");
        back.addActionListener(e -> dispose());
        bar.add(left, BorderLayout.WEST);
        bar.add(back, BorderLayout.EAST);
        return bar;
    }

    //BODY
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        //Stat cards
        lblTotalTransaksi  = new JLabel("0");
        lblTotalPendapatan = new JLabel("Rp 0");
        lblTotalItem       = new JLabel("0");

        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(buildStatCard("\uD83D\uDCCB", "Total Transaksi",    lblTotalTransaksi,  new Color(99, 102, 241)));
        stats.add(buildStatCard("\uD83D\uDCB0", "Total Pendapatan",   lblTotalPendapatan, C_SUCCESS));
        stats.add(buildStatCard("\uD83D\uDCE6", "Total Item Terjual", lblTotalItem,       C_ACCENT));

        body.add(stats,          BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        return body;
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
                g2.setColor(new Color(248, 250, 252)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(txtCari != null && txtCari.hasFocus() ? C_ACCENT : C_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        searchPanel.setOpaque(false); searchPanel.setPreferredSize(new Dimension(200, 34));
        txtCari = new JTextField(); txtCari.setOpaque(false);
        txtCari.setBorder(new EmptyBorder(4, 10, 4, 4));
        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 12)); txtCari.setForeground(new Color(30, 41, 59));
        JButton btnCari = new JButton("\uD83D\uDD0D");
        btnCari.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btnCari.setOpaque(false); btnCari.setContentAreaFilled(false);
        btnCari.setBorderPainted(false); btnCari.setFocusPainted(false);
        btnCari.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnCari.setPreferredSize(new Dimension(34, 34));
        searchPanel.add(txtCari, BorderLayout.CENTER); searchPanel.add(btnCari, BorderLayout.EAST);

        cmbFilter = new JComboBox<>(new String[]{"Semua Data", "Hari Ini", "7 Hari Terakhir", "Bulan Ini"});
        cmbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12)); cmbFilter.setPreferredSize(new Dimension(150, 34));

        JButton btnRefresh = buildSmallBtn("Refresh", C_PRIMARY);

        JPanel toolbarRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbarRight.setOpaque(false);
        toolbarRight.add(cmbFilter); toolbarRight.add(searchPanel); toolbarRight.add(btnRefresh);

        JLabel tblTitle = new JLabel("\uD83D\uDDC2  Riwayat Transaksi");
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); tblTitle.setForeground(C_PRIMARY);

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.add(tblTitle,    BorderLayout.WEST);
        toolbar.add(toolbarRight, BorderLayout.EAST);

        // tabel
        model = new DefaultTableModel(
            new String[]{"No","No Faktur","Tanggal","Customer","Nama Barang","Harga Satuan","Jumlah","Subtotal","Total Faktur"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblLaporan = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) { c.setBackground(new Color(224, 247, 250)); c.setForeground(C_PRIMARY); }
                else { c.setBackground(row%2==0 ? C_WHITE : C_ROW_ALT); c.setForeground(new Color(30, 41, 59)); }
                return c;
            }
        };
        tblLaporan.setFont(new Font("Segoe UI", Font.PLAIN, 13)); tblLaporan.setRowHeight(40);
        tblLaporan.setShowGrid(false); tblLaporan.setIntercellSpacing(new Dimension(0, 0));
        tblLaporan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tblLaporan.setFocusable(false);

        JTableHeader th = tblLaporan.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12)); th.setBackground(new Color(241, 245, 249)); th.setForeground(C_PRIMARY);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, C_ACCENT)); th.setPreferredSize(new Dimension(0, 42));
        ((DefaultTableCellRenderer) th.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // cell renderers
        DefaultTableCellRenderer padL = padRenderer(SwingConstants.LEFT, null, null);
        DefaultTableCellRenderer padR = padRenderer(SwingConstants.RIGHT, null, null);
        DefaultTableCellRenderer totalR = padRenderer(SwingConstants.RIGHT, C_SUCCESS, new Font("Segoe UI", Font.BOLD, 13));

        int[] widths = {40, 140, 150, 160, 160, 110, 70, 110, 120};
        int[] maxW   = {50,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1};
        DefaultTableCellRenderer[] renderers = {padL, padL, padL, padL, padL, padR, padR, padR, totalR};
        for (int i = 0; i < 9; i++) {
            tblLaporan.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            if (maxW[i] > 0) tblLaporan.getColumnModel().getColumn(i).setMaxWidth(maxW[i]);
            tblLaporan.getColumnModel().getColumn(i).setCellRenderer(renderers[i]);
        }

        JScrollPane scroll = new JScrollPane(tblLaporan);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));
        scroll.getViewport().setBackground(C_WHITE);

        JLabel lblJumlah = new JLabel("Memuat data...");
        lblJumlah.setFont(new Font("Segoe UI", Font.ITALIC, 11)); lblJumlah.setForeground(C_MUTED);
        model.addTableModelListener(e -> lblJumlah.setText("Menampilkan " + model.getRowCount() + " transaksi"));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false); footer.setBorder(new EmptyBorder(8, 2, 0, 2));
        footer.add(lblJumlah, BorderLayout.WEST);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(scroll,  BorderLayout.CENTER);
        card.add(footer,  BorderLayout.SOUTH);

        // events
        btnRefresh.addActionListener(e -> tampilData());
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
        cmbFilter.addActionListener(e -> filterData());

        return card;
    }

    //DATA OPERATIONS

    private void tampilData() {
        isiTabel(SQL_BASE + "ORDER BY p.tgl_transaksi DESC, p.id_jual ASC", (Object[]) null);
    }

    private void filterData() {
        String kondisi;
        switch (cmbFilter.getSelectedItem().toString()) {
            case "Hari Ini":        kondisi = "DATE(p.tgl_transaksi)=CURDATE()"; break;
            case "7 Hari Terakhir": kondisi = "p.tgl_transaksi>=DATE_SUB(NOW(),INTERVAL 7 DAY)"; break;
            case "Bulan Ini":       kondisi = "MONTH(p.tgl_transaksi)=MONTH(NOW()) AND YEAR(p.tgl_transaksi)=YEAR(NOW())"; break;
            default: tampilData(); return;
        }
        isiTabel(SQL_BASE + "WHERE " + kondisi + " ORDER BY p.tgl_transaksi DESC, p.id_jual ASC", (Object[]) null);
    }

    private void cariData() {
        String keyword = txtCari.getText().trim();
        if (keyword.isEmpty()) { tampilData(); return; }
        String k = "%" + keyword + "%";
        isiTabel(SQL_BASE +
            "WHERE p.no_faktur LIKE ? OR p.id_customer LIKE ? " +
            "OR c.nama_customer LIKE ? OR b.nama_barang LIKE ? OR p.tgl_transaksi LIKE ? " +
            "ORDER BY p.tgl_transaksi DESC",
            k, k, k, k, k);
    }

    /** Eksekusi SQL, isi tabel, update statistik. params=null → no PreparedStatement. */
    private void isiTabel(String sql, Object... params) {
        model.setRowCount(0);
        try {
            Connection conn = Koneksi.getKoneksi();
            ResultSet rs;
            if (params == null || params.length == 0) {
                rs = conn.createStatement().executeQuery(sql);
            } else {
                PreparedStatement pst = conn.prepareStatement(sql);
                for (int i = 0; i < params.length; i++) pst.setObject(i + 1, params[i]);
                rs = pst.executeQuery();
            }
            int no = 1;
            while (rs.next()) {
                String cust = rs.getString("nama_customer") != null
                    ? rs.getString("id_customer") + " — " + rs.getString("nama_customer")
                    : rs.getString("id_customer");
                model.addRow(new Object[]{
                    no++,
                    rs.getString("no_faktur"),
                    rs.getString("tgl_transaksi"),
                    cust,
                    rs.getString("nama_barang") != null ? rs.getString("nama_barang") : "-",
                    "Rp " + DF.format(rs.getDouble("harga_satuan")),
                    rs.getInt("jumlah_beli"),
                    "Rp " + DF.format(rs.getDouble("subtotal")),
                    "Rp " + DF.format(rs.getDouble("total_bayar"))
                });
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
        updateStatistik();
    }

    private void updateStatistik() {
        Set<String> fakturUnik = new HashSet<>();
        double totalPendapatan = 0; int totalItem = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            fakturUnik.add(model.getValueAt(i, 1).toString());
            try { totalPendapatan += Double.parseDouble(model.getValueAt(i, 7).toString().replaceAll("[^0-9]","")); } catch (Exception ignored) {}
            try { totalItem += Integer.parseInt(model.getValueAt(i, 6).toString()); } catch (Exception ignored) {}
        }
        lblTotalTransaksi.setText(String.valueOf(fakturUnik.size()));
        lblTotalPendapatan.setText("Rp " + DF.format(totalPendapatan));
        lblTotalItem.setText(String.valueOf(totalItem));
    }

    //HELPER BUILDERS

    private JPanel buildStatCard(String emoji, String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(accent);  g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.setColor(new Color(0, 0, 0, 8)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        card.setBorder(new EmptyBorder(18, 24, 18, 24));
        card.setPreferredSize(new Dimension(0, 90));

        JLabel ico = new JLabel(emoji); ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26)); ico.setForeground(accent);
        JLabel lbl = new JLabel(label); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11)); lbl.setForeground(C_MUTED);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22)); valueLabel.setForeground(C_PRIMARY);

        JPanel text = new JPanel(); text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(lbl); text.add(Box.createRigidArea(new Dimension(0, 2))); text.add(valueLabel);

        JPanel inner = new JPanel(new BorderLayout(14, 0)); inner.setOpaque(false);
        inner.add(ico,  BorderLayout.WEST);
        inner.add(text, BorderLayout.CENTER);
        card.add(inner);
        return card;
    }

    private JPanel buildCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(0, 0, 0, 8)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
    }

    /** Cell renderer dengan padding, alignment, dan warna/font opsional. */
    private DefaultTableCellRenderer padRenderer(int align, Color fg, Font font) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                setHorizontalAlignment(align);
                if (!sel && fg   != null) setForeground(fg);
                if (       font != null) setFont(font);
                return this;
            }
        };
    }

    private JButton buildTopBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,20)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(0,188,212,80)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); btn.setFocusPainted(false);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setPreferredSize(new Dimension(110, 36));
        return btn;
    }

    private JButton buildSmallBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,getBackground().brighter(),0,getHeight(),getBackground()));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setBackground(color); btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setPreferredSize(new Dimension(110, 34));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(color); btn.repaint(); }
        });
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormLaporan().setVisible(true));
    }
}