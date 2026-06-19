package tokohberkahjaya;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class FormTransaksi extends JFrame {

    //WARNA
    private static final Color C_PRIMARY = new Color(15,52,96),   C_ACCENT  = new Color(0,188,212),
        C_BG_DARK = new Color(10,25,47),   C_BG      = new Color(241,245,249),
        C_WHITE   = Color.WHITE,            C_BORDER  = new Color(226,232,240),
        C_MUTED   = new Color(100,116,139), C_SUCCESS = new Color(16,185,129),
        C_DANGER  = new Color(239,68,68),   C_ROW_ALT = new Color(248,250,252);
    private static final DecimalFormat DF = new DecimalFormat("#,##0");

    //KOMPONEN
    private JComboBox<String>  cmbCustomer, cmbBarang;
    private JTextField         txtJumlah, txtHargaSatuan, txtStokInfo;
    private JTable             tabelKeranjang;
    private DefaultTableModel  modelKeranjang;
    private JLabel             lblSubtotal, lblDiskon, lblTotal, lblJumlahItem, lblTanggal;
    private JButton            btnTambah, btnHapusItem, btnBayar, btnBatalTransaksi, btnBack;

    //KONSTRUKTOR
    public FormTransaksi() {
        setTitle("TOKO BERKAH JAYA — Transaksi Penjualan");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        loadCustomer();
        loadBarang();
        lblTanggal.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy  HH:mm").format(new Date()));
    }

    //BUILD UI
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);

        // top bar
        JPanel topBar = gradientPanel();
        topBar.setBorder(new EmptyBorder(16,28,16,28));
        topBar.setPreferredSize(new Dimension(0,70));
        btnBack = buildBtn("← Kembali", new Color(71,85,105));
        btnBack.setPreferredSize(new Dimension(110,35));
        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        topLeft.setOpaque(false);
        topLeft.add(btnBack);
        topLeft.add(Box.createRigidArea(new Dimension(15,0)));
        topLeft.add(lbl("\uD83D\uDCB0  ", new Font("Segoe UI Emoji",Font.PLAIN,22), C_ACCENT));
        topLeft.add(lbl("Transaksi Penjualan", new Font("Segoe UI",Font.BOLD,20), C_WHITE));
        topLeft.add(lbl("   Tambahkan barang ke keranjang lalu klik Bayar", new Font("Segoe UI",Font.PLAIN,12), new Color(150,190,220)));
        lblTanggal = lbl("", new Font("Segoe UI",Font.PLAIN,12), new Color(150,190,220));
        topBar.add(topLeft, BorderLayout.WEST);
        topBar.add(lblTanggal, BorderLayout.EAST);

        // body
        JPanel body = new JPanel(new BorderLayout(16,0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16,20,16,20));
        body.add(buildLeftPanel(),  BorderLayout.WEST);
        body.add(buildRightPanel(), BorderLayout.CENTER);

        root.add(topBar, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        add(root);

        // events
        cmbBarang.addActionListener(e -> onBarangSelected());
        btnTambah.addActionListener(e -> tambahKeKeranjang());
        btnHapusItem.addActionListener(e -> hapusItemKeranjang());
        btnBayar.addActionListener(e -> prosesTransaksi());
        btnBack.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this,"Kembali ke Menu Utama?","Konfirmasi",
                    JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) { new MenuUtama().setVisible(true); dispose(); }
        });
        btnBatalTransaksi.addActionListener(e -> {
            if (modelKeranjang.getRowCount()==0) { dispose(); return; }
            if (JOptionPane.showConfirmDialog(this,"Batalkan semua item di keranjang?","Konfirmasi",
                    JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION) bersihkanKeranjang();
        });
        txtJumlah.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) tambahKeKeranjang(); }
        });
    }

    //LEFT PANEL
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0,14));
        p.setOpaque(false); p.setPreferredSize(new Dimension(320,0));
        p.add(buildCardCustomer(),  BorderLayout.NORTH);
        p.add(buildCardBarang(),    BorderLayout.CENTER);
        p.add(buildCardRingkasan(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildCardCustomer() {
        JPanel card = buildCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18,20,18,20));
        cmbCustomer = buildCombo();
        cmbCustomer.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        cmbCustomer.setAlignmentX(LEFT_ALIGNMENT);
        addBox(card, cardTitle("Data Customer"), fieldLbl("Customer"), cmbCustomer);
        return card;
    }

    private JPanel buildCardBarang() {
        JPanel card = buildCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18,20,18,20));

        cmbBarang      = buildCombo();
        txtHargaSatuan = readonlyField("0");
        txtStokInfo    = readonlyField("0");
        txtJumlah      = inputField();

        cmbBarang.setMaximumSize(new Dimension(Integer.MAX_VALUE,40)); cmbBarang.setAlignmentX(LEFT_ALIGNMENT);
        txtJumlah.setMaximumSize(new Dimension(Integer.MAX_VALUE,40)); txtJumlah.setAlignmentX(LEFT_ALIGNMENT);

        btnTambah = buildBtn("+ Tambah ke Keranjang", C_ACCENT);
        btnTambah.setAlignmentX(LEFT_ALIGNMENT);
        btnTambah.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));

        // baris harga + stok
        JPanel rowHS = new JPanel(new GridLayout(1,2,10,0));
        rowHS.setOpaque(false); rowHS.setAlignmentX(LEFT_ALIGNMENT);
        rowHS.setMaximumSize(new Dimension(Integer.MAX_VALUE,60));
        for (String[] pair : new String[][]{{"Harga Satuan (Rp)", null},{"Stok Tersedia", null}}) {
            JTextField tf = pair[0].startsWith("H") ? txtHargaSatuan : txtStokInfo;
            JPanel col = new JPanel(new BorderLayout(0,4)); col.setOpaque(false);
            col.add(fieldLbl(pair[0]), BorderLayout.NORTH); col.add(tf, BorderLayout.CENTER);
            rowHS.add(col);
        }

        addBox(card, cardTitle("Tambah Barang"), fieldLbl("Pilih Barang"), cmbBarang);
        card.add(Box.createRigidArea(new Dimension(0,10)));
        card.add(rowHS);
        card.add(Box.createRigidArea(new Dimension(0,10)));
        card.add(fieldLbl("Jumlah")); card.add(Box.createRigidArea(new Dimension(0,4)));
        card.add(txtJumlah);          card.add(Box.createRigidArea(new Dimension(0,14)));
        card.add(btnTambah);
        return card;
    }

    private JPanel buildCardRingkasan() {
        JPanel card = buildCard();
        card.setLayout(new BorderLayout());

        // header gradient
        JPanel header = gradientPanel();
        header.setBorder(new EmptyBorder(14,20,14,20));
        header.setPreferredSize(new Dimension(0,50));
        JLabel title = lbl("Ringkasan Belanja", new Font("Segoe UI",Font.BOLD,13), C_WHITE);
        lblJumlahItem = lbl("0 item", new Font("Segoe UI",Font.PLAIN,12), new Color(150,200,230));
        header.add(title, BorderLayout.WEST); header.add(lblJumlahItem, BorderLayout.EAST);

        // body
        JPanel body = new JPanel(); body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(16,20,16,20));

        lblSubtotal = summaryRow(body, "Subtotal");
        lblDiskon   = summaryRow(body, "Diskon");

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,1)); sep.setForeground(C_BORDER);
        body.add(Box.createRigidArea(new Dimension(0,8))); body.add(sep); body.add(Box.createRigidArea(new Dimension(0,8)));

        JPanel rowTotal = new JPanel(new BorderLayout()); rowTotal.setOpaque(false);
        rowTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        JLabel lTotalLbl = lbl("TOTAL", new Font("Segoe UI",Font.BOLD,15), C_PRIMARY);
        lblTotal = lbl("Rp 0", new Font("Segoe UI",Font.BOLD,18), C_ACCENT);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        rowTotal.add(lTotalLbl, BorderLayout.WEST); rowTotal.add(lblTotal, BorderLayout.EAST);
        body.add(rowTotal); body.add(Box.createRigidArea(new Dimension(0,16)));

        btnBayar = buildBtn("BAYAR SEKARANG", C_SUCCESS);
        btnBayar.setAlignmentX(LEFT_ALIGNMENT); btnBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));
        btnBayar.setFont(new Font("Segoe UI",Font.BOLD,14));
        body.add(btnBayar); body.add(Box.createRigidArea(new Dimension(0,8)));

        btnBatalTransaksi = buildBtn("Batalkan Transaksi", new Color(100,116,139));
        btnBatalTransaksi.setAlignmentX(LEFT_ALIGNMENT); btnBatalTransaksi.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        body.add(btnBatalTransaksi);

        card.add(header, BorderLayout.NORTH); card.add(body, BorderLayout.CENTER);
        return card;
    }

    //RIGHT PANEL (KERANJANG)
    private JPanel buildRightPanel() {
        JPanel panel = buildCard();
        panel.setLayout(new BorderLayout(0,12));
        panel.setBorder(new EmptyBorder(20,20,20,20));

        btnHapusItem = buildBtn("Hapus Item", C_DANGER);
        btnHapusItem.setPreferredSize(new Dimension(130,36));
        btnHapusItem.setFont(new Font("Segoe UI",Font.BOLD,12));

        JLabel tblTitle = lbl("Keranjang Belanja", new Font("Segoe UI",Font.BOLD,15), C_PRIMARY);
        JPanel tblHeader = new JPanel(new BorderLayout(12,0)); tblHeader.setOpaque(false);
        tblHeader.add(tblTitle, BorderLayout.WEST); tblHeader.add(btnHapusItem, BorderLayout.EAST);

        modelKeranjang = new DefaultTableModel(
            new String[]{"No","Kode Barang","Nama Barang","Harga Satuan","Jumlah","Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r,int c) { return false; }
        };
        tabelKeranjang = new JTable(modelKeranjang) {
            @Override public Component prepareRenderer(TableCellRenderer r,int row,int col) {
                Component c = super.prepareRenderer(r,row,col);
                if (isRowSelected(row)) { c.setBackground(new Color(224,247,250)); c.setForeground(C_PRIMARY); }
                else { c.setBackground(row%2==0?C_WHITE:C_ROW_ALT); c.setForeground(new Color(30,41,59)); }
                return c;
            }
        };
        tabelKeranjang.setFont(new Font("Segoe UI",Font.PLAIN,13)); tabelKeranjang.setRowHeight(40);
        tabelKeranjang.setShowGrid(false); tabelKeranjang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelKeranjang.setFocusable(false);

        JTableHeader th = tabelKeranjang.getTableHeader();
        th.setFont(new Font("Segoe UI",Font.BOLD,12)); th.setBackground(new Color(241,245,249)); th.setForeground(C_PRIMARY);
        th.setBorder(BorderFactory.createMatteBorder(0,0,2,0,C_ACCENT)); th.setPreferredSize(new Dimension(0,42));
        ((DefaultTableCellRenderer)th.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer padL = padRenderer(false), padR = padRenderer(true);
        for (int i=0;i<4;i++) tabelKeranjang.getColumnModel().getColumn(i).setCellRenderer(padL);
        tabelKeranjang.getColumnModel().getColumn(4).setCellRenderer(padR);
        tabelKeranjang.getColumnModel().getColumn(5).setCellRenderer(padR);
        tabelKeranjang.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scroll = new JScrollPane(tabelKeranjang);
        scroll.setBorder(BorderFactory.createMatteBorder(1,0,0,0,C_BORDER));
        scroll.getViewport().setBackground(C_WHITE);

        JPanel emptyHint = new JPanel(new GridBagLayout()); emptyHint.setBackground(C_WHITE);
        JLabel emptyLbl = new JLabel("<html><center>\uD83D\uDED2<br><br>Keranjang masih kosong<br>"
            +"<span style='color:#94a3b8;font-size:11px'>Pilih barang dan klik + Tambah ke Keranjang</span></center></html>");
        emptyLbl.setFont(new Font("Segoe UI",Font.PLAIN,14)); emptyLbl.setForeground(C_MUTED);
        emptyHint.add(emptyLbl);

        JPanel tableArea = new JPanel(new CardLayout());
        tableArea.add(emptyHint,"empty"); tableArea.add(scroll,"table");
        ((CardLayout)tableArea.getLayout()).show(tableArea,"empty");
        modelKeranjang.addTableModelListener(e ->
            ((CardLayout)tableArea.getLayout()).show(tableArea, modelKeranjang.getRowCount()==0?"empty":"table"));

        panel.add(tblHeader, BorderLayout.NORTH);
        panel.add(tableArea,  BorderLayout.CENTER);
        return panel;
    }

    //DATA OPERATIONS
    private void loadCustomer() {
        try {
            cmbCustomer.removeAllItems();
            ResultSet rs = Koneksi.getKoneksi().createStatement()
                .executeQuery("SELECT id_customer,nama_customer FROM customer");
            while (rs.next()) cmbCustomer.addItem(rs.getString(1)+" - "+rs.getString(2));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadBarang() {
        try {
            cmbBarang.removeAllItems(); cmbBarang.addItem("-- Pilih Barang --");
            ResultSet rs = Koneksi.getKoneksi().createStatement()
                .executeQuery("SELECT id_barang,nama_barang FROM barang WHERE stok>0");
            while (rs.next()) cmbBarang.addItem(rs.getString(1)+" | "+rs.getString(2));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void onBarangSelected() {
        if (cmbBarang.getSelectedIndex()<=0) { txtHargaSatuan.setText("0"); txtStokInfo.setText("0"); return; }
        try {
            String kode = cmbBarang.getSelectedItem().toString().split("\\|")[0].trim();
            PreparedStatement pst = Koneksi.getKoneksi().prepareStatement(
                "SELECT harga_jual,stok FROM barang WHERE id_barang=?");
            pst.setString(1,kode); ResultSet rs = pst.executeQuery();
            if (rs.next()) { txtHargaSatuan.setText(String.valueOf(rs.getDouble(1))); txtStokInfo.setText(String.valueOf(rs.getInt(2))); }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void tambahKeKeranjang() {
        if (cmbBarang.getSelectedIndex()<=0) { warn("Pilih barang terlebih dahulu!"); return; }
        String jumlahStr = txtJumlah.getText().trim();
        if (jumlahStr.isEmpty()) { warn("Isi jumlah barang!"); return; }
        int jumlah; try { jumlah=Integer.parseInt(jumlahStr); } catch (NumberFormatException e) { warn("Jumlah harus berupa angka!"); return; }
        if (jumlah<=0) { warn("Jumlah harus lebih dari 0!"); return; }
        int stok; try { stok=Integer.parseInt(txtStokInfo.getText().trim()); } catch (Exception e) { stok=0; }
        if (jumlah>stok) { warn("Stok tidak mencukupi! Stok tersedia: "+stok); return; }

        String[] parts = cmbBarang.getSelectedItem().toString().split("\\|");
        String kode = parts[0].trim(), nama = parts.length>1 ? parts[1].trim() : parts[0].trim();
        double harga; try { harga=Double.parseDouble(txtHargaSatuan.getText().trim()); } catch (Exception e) { warn("Harga tidak valid!"); return; }

        for (int i=0;i<modelKeranjang.getRowCount();i++) {
            if (modelKeranjang.getValueAt(i,1).toString().equals(kode)) {
                int baru = Integer.parseInt(modelKeranjang.getValueAt(i,4).toString())+jumlah;
                if (baru>stok) { warn("Total melebihi stok!"); return; }
                modelKeranjang.setValueAt(baru,i,4);
                modelKeranjang.setValueAt("Rp "+DF.format(harga*baru),i,5);
                updateRingkasan(); txtJumlah.setText(""); txtJumlah.requestFocus(); return;
            }
        }
        modelKeranjang.addRow(new Object[]{modelKeranjang.getRowCount()+1, kode, nama,
            "Rp "+DF.format(harga), jumlah, "Rp "+DF.format(harga*jumlah)});
        updateRingkasan();
        txtJumlah.setText(""); cmbBarang.setSelectedIndex(0);
        txtHargaSatuan.setText("0"); txtStokInfo.setText("0"); txtJumlah.requestFocus();
    }

    private void hapusItemKeranjang() {
        int row = tabelKeranjang.getSelectedRow();
        if (row<0) { warn("Pilih item yang akan dihapus!"); return; }
        modelKeranjang.removeRow(row);
        for (int i=0;i<modelKeranjang.getRowCount();i++) modelKeranjang.setValueAt(i+1,i,0);
        updateRingkasan();
    }

    private void updateRingkasan() {
        double sub = 0;
        for (int i=0;i<modelKeranjang.getRowCount();i++) {
            try { sub+=Double.parseDouble(modelKeranjang.getValueAt(i,5).toString().replaceAll("[^0-9]","")); } catch (Exception ignored) {}
        }
        lblSubtotal.setText("Rp "+DF.format(sub));
        lblDiskon.setText("Rp 0");
        lblTotal.setText("Rp "+DF.format(sub));
        lblJumlahItem.setText(modelKeranjang.getRowCount()+" item");
    }

    private String generateNoFaktur(Connection conn) throws SQLException {
        String tgl = new SimpleDateFormat("yyyyMMdd").format(new Date());
        PreparedStatement pst = conn.prepareStatement(
            "SELECT no_faktur FROM penjualan WHERE no_faktur LIKE ? ORDER BY no_faktur DESC LIMIT 1");
        pst.setString(1,"FK"+tgl+"%"); ResultSet rs = pst.executeQuery();
        int urut = rs.next() ? Integer.parseInt(rs.getString("no_faktur").substring(10))+1 : 1;
        return String.format("FK%s%03d",tgl,urut);
    }

    private double showDialogPembayaran(double total) {
        JDialog dlg = new JDialog(this,"Pembayaran",true);
        dlg.setSize(380,370); dlg.setLocationRelativeTo(this); dlg.setResizable(false);

        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24,28,24,28)); p.setBackground(C_WHITE);

        JLabel lblTot = lbl("Rp "+DF.format(total), new Font("Segoe UI",Font.BOLD,24), C_PRIMARY);
        JLabel lblKem = lbl("Rp 0",                 new Font("Segoe UI",Font.BOLD,20), C_SUCCESS);
        lblKem.setAlignmentX(LEFT_ALIGNMENT);

        JTextField txtBayar = new JTextField();
        txtBayar.setFont(new Font("Segoe UI",Font.PLAIN,16));
        txtBayar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_BORDER),BorderFactory.createEmptyBorder(8,12,8,12)));
        txtBayar.setAlignmentX(LEFT_ALIGNMENT); txtBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));

        final double[] hasil = {-1};
        txtBayar.getDocument().addDocumentListener(new DocumentListener() {
            void update() {
                try {
                    double bayar = Double.parseDouble(txtBayar.getText().trim().replaceAll("[^0-9]",""));
                    double kem   = bayar - total;
                    lblKem.setText(kem<0 ? "Kurang Rp "+DF.format(Math.abs(kem)) : "Rp "+DF.format(kem));
                    lblKem.setForeground(kem<0 ? C_DANGER : C_SUCCESS);
                } catch (Exception ex) { lblKem.setText("Rp 0"); lblKem.setForeground(C_SUCCESS); }
            }
            @Override public void insertUpdate(DocumentEvent e)  { update(); }
            @Override public void removeUpdate(DocumentEvent e)  { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        JButton btnProses = buildBtn("PROSES PEMBAYARAN", C_SUCCESS);
        btnProses.setAlignmentX(LEFT_ALIGNMENT); btnProses.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));
        btnProses.addActionListener(e -> {
            try {
                double bayar = Double.parseDouble(txtBayar.getText().trim().replaceAll("[^0-9]",""));
                if (bayar<total) { JOptionPane.showMessageDialog(dlg,"Uang kurang! Kurang: Rp "+DF.format(total-bayar),"Peringatan",JOptionPane.WARNING_MESSAGE); return; }
                hasil[0]=bayar; dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg,"Nominal tidak valid!","Peringatan",JOptionPane.WARNING_MESSAGE); }
        });
        JButton btnBatal = buildBtn("Batal", new Color(100,116,139));
        btnBatal.setAlignmentX(LEFT_ALIGNMENT); btnBatal.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        btnBatal.addActionListener(e -> dlg.dispose());
        txtBayar.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) btnProses.doClick(); }
        });

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,1)); sep.setForeground(C_BORDER);

        // susun komponen dialog
        Object[][] rows = {
            {lbl("Total Belanja",new Font("Segoe UI",Font.PLAIN,12),C_MUTED), 6},
            {lblTot, 6}, {sep, 12},
            {lbl("Uang yang Dibayar (Rp)",new Font("Segoe UI",Font.BOLD,12),new Color(51,65,85)), 6},
            {txtBayar, 6},
            {lbl("Kembalian",new Font("Segoe UI",Font.PLAIN,12),C_MUTED), 6},
            {lblKem, 6}, {btnProses, 6}, {btnBatal, 6}
        };
        for (Object[] row : rows) {
            Component c = (Component)row[0];
            if (c instanceof JLabel) ((JLabel)c).setAlignmentX(LEFT_ALIGNMENT);
            p.add(c); p.add(Box.createRigidArea(new Dimension(0,(int)row[1])));
        }

        dlg.setContentPane(p); dlg.setVisible(true);
        return hasil[0];
    }

    private void prosesTransaksi() {
        if (cmbCustomer.getSelectedItem()==null) { warn("Pilih customer!"); return; }
        if (modelKeranjang.getRowCount()==0)     { warn("Keranjang masih kosong!"); return; }

        String customerRaw = cmbCustomer.getSelectedItem().toString();
        String idCustomer  = customerRaw.split(" - ")[0].trim();
        double grandTotal  = 0;
        for (int i=0;i<modelKeranjang.getRowCount();i++) {
            try { grandTotal+=Double.parseDouble(modelKeranjang.getValueAt(i,5).toString().replaceAll("[^0-9]","")); } catch (Exception ignored) {}
        }

        double bayar = showDialogPembayaran(grandTotal);
        if (bayar<0) return;

        try {
            Connection conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);
            String noFaktur = generateNoFaktur(conn);

            for (int i=0;i<modelKeranjang.getRowCount();i++) {
                String kode = modelKeranjang.getValueAt(i,1).toString();
                int jumlah  = Integer.parseInt(modelKeranjang.getValueAt(i,4).toString());
                PreparedStatement cek = conn.prepareStatement("SELECT stok FROM barang WHERE id_barang=?");
                cek.setString(1,kode); ResultSet rs = cek.executeQuery();
                if (rs.next()&&rs.getInt("stok")<jumlah) { conn.rollback(); warn("Stok "+kode+" tidak mencukupi!"); return; }
            }

            PreparedStatement h = conn.prepareStatement(
                "INSERT INTO penjualan(no_faktur,tgl_transaksi,id_customer,total_bayar,id_user) VALUES(?,NOW(),?,?,1)",
                Statement.RETURN_GENERATED_KEYS);
            h.setString(1,noFaktur); h.setString(2,idCustomer); h.setDouble(3,grandTotal);
            h.executeUpdate();
            ResultSet keys = h.getGeneratedKeys();
            int idJual = keys.next() ? keys.getInt(1) : 0;

            for (int i=0;i<modelKeranjang.getRowCount();i++) {
                String kode = modelKeranjang.getValueAt(i,1).toString();
                int jumlah  = Integer.parseInt(modelKeranjang.getValueAt(i,4).toString());
                double hs   = Double.parseDouble(modelKeranjang.getValueAt(i,3).toString().replaceAll("[^0-9]",""));
                double sub  = Double.parseDouble(modelKeranjang.getValueAt(i,5).toString().replaceAll("[^0-9]",""));

                PreparedStatement d = conn.prepareStatement(
                    "INSERT INTO detail_penjualan(id_jual,id_barang,harga_satuan,jumlah_beli,subtotal) VALUES(?,?,?,?,?)");
                d.setInt(1,idJual); d.setString(2,kode); d.setDouble(3,hs); d.setInt(4,jumlah); d.setDouble(5,sub);
                d.executeUpdate();

                PreparedStatement s = conn.prepareStatement("UPDATE barang SET stok=stok-? WHERE id_barang=?");
                s.setInt(1,jumlah); s.setString(2,kode); s.executeUpdate();
            }

            conn.commit(); conn.setAutoCommit(true);
            JOptionPane.showMessageDialog(this,
                "<html><div style='font-family:Segoe UI'><b>Transaksi Berhasil!</b><br><br>"
                +"No. Faktur : <b>"+noFaktur+"</b><br>Customer : <b>"+customerRaw+"</b><br>"
                +"Total : <b>Rp "+DF.format(grandTotal)+"</b><br>Bayar : <b>Rp "+DF.format(bayar)+"</b><br>"
                +"<span style='color:#10b981'>Kembalian : <b>Rp "+DF.format(bayar-grandTotal)+"</b></span></div></html>",
                "Berhasil",JOptionPane.INFORMATION_MESSAGE);
            bersihkanKeranjang(); loadBarang();

        } catch (Exception ex) {
            try { Koneksi.getKoneksi().rollback(); } catch (Exception ignored) {}
            JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Gagal",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bersihkanKeranjang() {
        modelKeranjang.setRowCount(0); updateRingkasan();
        cmbBarang.setSelectedIndex(0);
        txtJumlah.setText(""); txtHargaSatuan.setText("0"); txtStokInfo.setText("0");
    }

    //HELPER BUILDERS

    private JPanel gradientPanel() {
        return new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,C_BG_DARK,getWidth(),0,C_PRIMARY));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
    }

    private JPanel buildCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(new Color(0,0,0,8)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16); g2.dispose();
            }
        };
    }

    private JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text); l.setFont(font); l.setForeground(color); return l;
    }
    private JLabel cardTitle(String t)  { return lbl(t, new Font("Segoe UI",Font.BOLD,14), C_PRIMARY); }
    private JLabel fieldLbl(String t)   { JLabel l=lbl(t,new Font("Segoe UI",Font.BOLD,11),new Color(51,65,85)); l.setAlignmentX(LEFT_ALIGNMENT); return l; }

    private void addBox(JPanel box, JLabel title, JLabel label, JComponent field) {
        title.setAlignmentX(LEFT_ALIGNMENT);
        box.add(title); box.add(Box.createRigidArea(new Dimension(0,10)));
        box.add(label); box.add(Box.createRigidArea(new Dimension(0,4))); box.add(field);
    }

    private JComboBox<String> buildCombo() {
        JComboBox<String> c = new JComboBox<>();
        c.setFont(new Font("Segoe UI",Font.PLAIN,13)); c.setBackground(new Color(248,250,252));
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_BORDER,1),BorderFactory.createEmptyBorder(4,8,4,8)));
        return c;
    }

    private JTextField inputField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI",Font.PLAIN,13));
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_BORDER),BorderFactory.createEmptyBorder(6,12,6,12)));
        f.setBackground(new Color(248,250,252)); return f;
    }
    private JTextField readonlyField(String val) { JTextField f=inputField(); f.setText(val); f.setEditable(false); f.setForeground(C_MUTED); return f; }

    private JButton buildBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,getBackground().brighter(),0,getHeight(),getBackground()));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(255,255,255,35)); g2.fillRoundRect(2,2,getWidth()-4,getHeight()/2,8,8);
                g2.setColor(Color.WHITE); g2.setFont(getFont()); FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setBackground(color); btn.setFont(new Font("Segoe UI",Font.BOLD,13));
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(color); btn.repaint(); }
        });
        return btn;
    }

    private DefaultTableCellRenderer padRenderer(boolean right) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c) {
                super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                setBorder(new EmptyBorder(0,14,0,14));
                if (right) setHorizontalAlignment(SwingConstants.RIGHT);
                return this;
            }
        };
    }

    private JLabel summaryRow(JPanel parent, String labelText) {
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,26));
        JLabel lbl = lbl(labelText,new Font("Segoe UI",Font.PLAIN,12),C_MUTED);
        JLabel val = lbl("Rp 0",  new Font("Segoe UI",Font.PLAIN,12),new Color(30,41,59));
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(lbl,BorderLayout.WEST); row.add(val,BorderLayout.EAST);
        parent.add(row); parent.add(Box.createRigidArea(new Dimension(0,4)));
        return val;
    }

    private void warn(String msg) { JOptionPane.showMessageDialog(this,msg,"Peringatan",JOptionPane.WARNING_MESSAGE); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormTransaksi().setVisible(true));
    }
}