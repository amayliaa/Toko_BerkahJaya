package tokohberkahjaya;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class FormDataBarang extends JFrame {

    // ── WARNA ─────────────────────────────────────────────────────────────────
    private static final Color C_PRIMARY  = new Color(15, 52, 96),
        C_ACCENT   = new Color(0, 188, 212),  C_BG_DARK = new Color(10, 25, 47),
        C_BG       = new Color(241, 245, 249), C_BORDER  = new Color(226, 232, 240),
        C_FOCUS    = new Color(0, 188, 212),   C_DANGER  = new Color(239, 68, 68),
        C_ERR_BG   = new Color(255, 241, 241);

    // ── KOMPONEN ──────────────────────────────────────────────────────────────
    private JTextField txtKode, txtNama, txtHarga, txtStok, txtSatuan, txtCari;
    private JLabel     lblHargaError;
    private boolean    hargaValid = true;
    private JComboBox<KategoriItem> cmbKategori;
    private JButton    btnSimpan, btnHapus, btnBatal, btnCari;
    private JTable     tabel;
    private DefaultTableModel modelTabel;

    // ── INNER CLASS ───────────────────────────────────────────────────────────
    private static class KategoriItem {
        final String id, nama;
        KategoriItem(String id, String nama) { this.id = id; this.nama = nama; }
        @Override public String toString() { return nama; }
    }

    // ── FORMAT ────────────────────────────────────────────────────────────────
    private static String toRp(double v)       { return String.format("Rp %,.0f", v).replace(',', '.'); }
    private static long   parseRp(String s)    { String b = s.replaceAll("[^0-9]",""); if(b.isEmpty()) throw new NumberFormatException(); return Long.parseLong(b); }

    // ── KONSTRUKTOR ───────────────────────────────────────────────────────────
    public FormDataBarang(boolean isAdmin) {
        setTitle("TOKO BERKAH JAYA — Data Barang");
        setSize(1180, 700);
        setMinimumSize(new Dimension(960, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(C_BG); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(0,188,212,7));
                for (int i=0;i<getWidth();i+=28) for(int j=0;j<getHeight();j+=28) g2.fillOval(i,j,3,3);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildBody(isAdmin), BorderLayout.CENTER);
        add(root);

        loadKategori();
        loadData();
        txtKode.setText(generateKode());
    }

    // ── TOP BAR ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,C_BG_DARK,getWidth(),0,C_PRIMARY));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(0,188,212,20)); g2.fillOval(getWidth()-100,-30,140,140);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(18,28,18,28));
        bar.setPreferredSize(new Dimension(0,72));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        left.setOpaque(false);
        JLabel ico = new JLabel("\uD83D\uDCE6  "); ico.setFont(new Font("Segoe UI Emoji",Font.PLAIN,22)); ico.setForeground(C_ACCENT);
        JLabel ttl = new JLabel("Data Barang");    ttl.setFont(new Font("Segoe UI",Font.BOLD,20));        ttl.setForeground(Color.WHITE);
        JLabel sub = new JLabel("   Kelola inventaris toko Anda"); sub.setFont(new Font("Segoe UI",Font.PLAIN,12)); sub.setForeground(new Color(150,190,220));
        left.add(ico); left.add(ttl); left.add(sub);

        JButton back = buildTopBtn("\u2190  Kembali");
        back.addActionListener(e -> dispose());
        bar.add(left, BorderLayout.WEST);
        bar.add(back, BorderLayout.EAST);
        return bar;
    }

    // ── BODY ──────────────────────────────────────────────────────────────────
    private JPanel buildBody(boolean isAdmin) {
        JPanel body = new JPanel(new BorderLayout(16,0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20,20,20,20));
        body.add(buildFormPanel(isAdmin), BorderLayout.WEST);
        body.add(buildTablePanel(),       BorderLayout.CENTER);
        return body;
    }

    // ── FORM PANEL ────────────────────────────────────────────────────────────
    private JPanel buildFormPanel(boolean isAdmin) {
        JPanel p = buildCard(18);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24,24,24,24));
        p.setPreferredSize(new Dimension(300,0));

        JLabel title = new JLabel("Form Input Barang");
        title.setFont(new Font("Segoe UI",Font.BOLD,15)); title.setForeground(C_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        // fields
        txtKode   = buildField(); txtKode.setEditable(false); txtKode.setForeground(new Color(100,116,139)); txtKode.setBackground(C_BG);
        txtNama   = buildField();
        txtHarga  = buildField();
        txtStok   = buildField();
        txtSatuan = buildField();
        for (JTextField f : new JTextField[]{txtKode,txtNama,txtHarga,txtStok,txtSatuan}) {
            f.setAlignmentX(LEFT_ALIGNMENT); f.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        }

        lblHargaError = new JLabel(" ");
        lblHargaError.setFont(new Font("Segoe UI",Font.PLAIN,11)); lblHargaError.setForeground(C_DANGER);
        lblHargaError.setAlignmentX(LEFT_ALIGNMENT); lblHargaError.setMaximumSize(new Dimension(Integer.MAX_VALUE,16));
        txtHarga.getDocument().addDocumentListener((SimpleDocumentListener) e -> validasiHarga());

        cmbKategori = new JComboBox<>();
        cmbKategori.setFont(new Font("Segoe UI",Font.PLAIN,13));
        cmbKategori.setAlignmentX(LEFT_ALIGNMENT); cmbKategori.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        cmbKategori.setBackground(new Color(248,250,252)); cmbKategori.setForeground(new Color(30,41,59));
        cmbKategori.setBorder(BorderFactory.createLineBorder(C_BORDER,1,true));

        btnSimpan = buildBtn("Simpan", C_ACCENT);
        btnHapus  = buildBtn("Hapus",  C_DANGER);
        btnBatal  = buildBtn("Batal",  new Color(100,116,139));
        for (JButton b : new JButton[]{btnSimpan,btnHapus,btnBatal}) {
            b.setAlignmentX(LEFT_ALIGNMENT); b.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        }

        JPanel btnRow = new JPanel(new GridLayout(1,2,8,0));
        btnRow.setOpaque(false); btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        btnRow.add(btnHapus); btnRow.add(btnBatal);

        p.add(title); p.add(gap(8)); p.add(accentLine()); p.add(gap(16));
        addRow(p, "Kode Barang", txtKode,   null);
        addRow(p, "Nama Barang", txtNama,   null);
        addRow(p, "Harga Jual",  txtHarga,  lblHargaError);
        addRow(p, "Stok",        txtStok,   null);
        addRow(p, "Satuan",      txtSatuan, null);
        addRowKategori(p, isAdmin);
        p.add(gap(20)); p.add(btnSimpan); p.add(gap(8)); p.add(btnRow);
        p.add(Box.createVerticalGlue());

        // admin mode
        if (!isAdmin) {
            btnSimpan.setVisible(false); btnHapus.setVisible(false);
            for (JTextField f : new JTextField[]{txtKode,txtNama,txtHarga,txtStok,txtSatuan}) f.setEditable(false);
            cmbKategori.setEnabled(false);
        }

        // events
        btnSimpan.addActionListener(e -> simpan());
        btnHapus.addActionListener(e  -> hapus());
        btnBatal.addActionListener(e  -> bersihkan());

        return p;
    }

    // ── TABLE PANEL ───────────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel p = buildCard(18);
        p.setLayout(new BorderLayout(0,12));
        p.setBorder(new EmptyBorder(20,20,20,20));

        // search bar
        JPanel searchBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248,250,252)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                boolean foc = txtCari!=null && txtCari.hasFocus();
                g2.setColor(foc?C_ACCENT:C_BORDER); g2.setStroke(new BasicStroke(foc?1.5f:1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
            }
        };
        searchBar.setOpaque(false); searchBar.setPreferredSize(new Dimension(220,36));
        txtCari = new JTextField(); txtCari.setOpaque(false);
        txtCari.setBorder(new EmptyBorder(4,10,4,4)); txtCari.setFont(new Font("Segoe UI",Font.PLAIN,13));
        txtCari.setForeground(new Color(30,41,59));
        btnCari = new JButton("\uD83D\uDD0D");
        btnCari.setFont(new Font("Segoe UI Emoji",Font.PLAIN,13));
        btnCari.setOpaque(false); btnCari.setContentAreaFilled(false);
        btnCari.setBorderPainted(false); btnCari.setFocusPainted(false);
        btnCari.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnCari.setPreferredSize(new Dimension(36,36));
        searchBar.add(txtCari, BorderLayout.CENTER); searchBar.add(btnCari, BorderLayout.EAST);

        JLabel tblTitle = new JLabel("Daftar Barang");
        tblTitle.setFont(new Font("Segoe UI",Font.BOLD,15)); tblTitle.setForeground(C_PRIMARY);
        JPanel tblHeader = new JPanel(new BorderLayout(12,0)); tblHeader.setOpaque(false);
        tblHeader.add(tblTitle, BorderLayout.WEST); tblHeader.add(searchBar, BorderLayout.EAST);

        // tabel
        modelTabel = new DefaultTableModel(new String[]{"Kode","Nama Barang","Harga Jual","Stok","Satuan","Kategori"},0) {
            @Override public boolean isCellEditable(int r,int c) { return false; }
        };
        tabel = new JTable(modelTabel) {
            @Override public Component prepareRenderer(TableCellRenderer r,int row,int col) {
                Component c = super.prepareRenderer(r,row,col);
                if (isRowSelected(row)) { c.setBackground(new Color(224,247,250)); c.setForeground(C_PRIMARY); }
                else { c.setBackground(row%2==0?Color.WHITE:new Color(248,250,252)); c.setForeground(new Color(30,41,59)); }
                return c;
            }
        };
        tabel.setFont(new Font("Segoe UI",Font.PLAIN,13)); tabel.setRowHeight(38);
        tabel.setShowGrid(false); tabel.setIntercellSpacing(new Dimension(0,0));
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabel.setFocusable(false);

        JTableHeader th = tabel.getTableHeader();
        th.setFont(new Font("Segoe UI",Font.BOLD,12)); th.setBackground(new Color(241,245,249)); th.setForeground(C_PRIMARY);
        th.setBorder(BorderFactory.createMatteBorder(0,0,2,0,C_ACCENT)); th.setPreferredSize(new Dimension(0,40));
        ((DefaultTableCellRenderer)th.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer pad = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c) {
                super.getTableCellRendererComponent(t,v,s,f,r,c); setBorder(new EmptyBorder(0,14,0,14)); return this;
            }
        };
        for (int i=0;i<6;i++) tabel.getColumnModel().getColumn(i).setCellRenderer(pad);

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setBorder(BorderFactory.createMatteBorder(1,0,0,0,C_BORDER));
        scroll.getViewport().setBackground(Color.WHITE);

        p.add(tblHeader, BorderLayout.NORTH);
        p.add(scroll,    BorderLayout.CENTER);

        // events
        tabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = tabel.getSelectedRow();
                if (row < 0) return;
                txtKode.setText(modelTabel.getValueAt(row,0).toString());
                txtNama.setText(modelTabel.getValueAt(row,1).toString());
                txtHarga.setText(modelTabel.getValueAt(row,2).toString().replaceAll("[^0-9]",""));
                txtStok.setText(modelTabel.getValueAt(row,3).toString());
                txtSatuan.setText(modelTabel.getValueAt(row,4).toString());
                String kat = modelTabel.getValueAt(row,5).toString();
                for (int i=0;i<cmbKategori.getItemCount();i++)
                    if (cmbKategori.getItemAt(i).nama.equalsIgnoreCase(kat)) { cmbKategori.setSelectedIndex(i); break; }
            }
        });
        btnCari.addActionListener(e -> cari());
        txtCari.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) cari(); }
        });
        txtCari.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { searchBar.repaint(); }
            @Override public void focusLost(FocusEvent e)   { searchBar.repaint(); }
        });

        return p;
    }

    // ── VALIDASI HARGA ────────────────────────────────────────────────────────
    private void validasiHarga() {
        String val = txtHarga.getText().trim();
        if (val.isEmpty()) { setHargaErr(null); hargaValid=true; }
        else {
            String digits = val.replaceAll("[^0-9]","");
            if (val.matches(".*[a-zA-Z]+.*"))  { setHargaErr("Harga tidak boleh mengandung huruf"); hargaValid=false; }
            else if (digits.isEmpty())          { setHargaErr("\u26A0 Masukkan angka untuk harga jual"); hargaValid=false; }
            else {
                try {
                    long n = Long.parseLong(digits);
                    if (n<=0) { setHargaErr("\u26A0 Harga harus lebih dari 0"); hargaValid=false; }
                    else      { setHargaErr(null); hargaValid=true; }
                } catch (NumberFormatException ex) { setHargaErr("\u26A0 Angka terlalu besar"); hargaValid=false; }
            }
        }
        btnSimpan.setEnabled(hargaValid);
        btnSimpan.setBackground(hargaValid ? C_ACCENT : new Color(180,180,180));
        btnSimpan.repaint();
    }

    private void setHargaErr(String msg) {
        lblHargaError.setText(msg==null?" ":msg);
        txtHarga.putClientProperty("errorState", msg!=null);
        txtHarga.repaint();
    }

    // ── DATA OPERATIONS ───────────────────────────────────────────────────────
    private void loadKategori() {
        cmbKategori.removeAllItems();
        try {
            ResultSet rs = Koneksi.getKoneksi().prepareStatement(
                "SELECT id_kategori,nama_kategori FROM kategori ORDER BY nama_kategori ASC").executeQuery();
            while (rs.next()) cmbKategori.addItem(new KategoriItem(rs.getString(1),rs.getString(2)));
        } catch (Exception e) { JOptionPane.showMessageDialog(this,"Error load kategori: "+e.getMessage()); }
    }

    private void loadData() {
        modelTabel.setRowCount(0);
        try {
            ResultSet rs = Koneksi.getKoneksi().prepareStatement(
                "SELECT b.id_barang,b.nama_barang,b.harga_jual,b.stok,b.satuan," +
                "COALESCE(k.nama_kategori,'-') AS nama_kategori " +
                "FROM barang b LEFT JOIN kategori k ON b.id_kategori=k.id_kategori " +
                "WHERE b.status='aktif' ORDER BY b.id_barang ASC").executeQuery();
            while (rs.next())
                modelTabel.addRow(new Object[]{rs.getString(1),rs.getString(2),
                    toRp(rs.getDouble(3)),rs.getString(4),rs.getString(5),rs.getString(6)});
        } catch (Exception e) { JOptionPane.showMessageDialog(this,"Error load data: "+e.getMessage()); }
    }

    private void simpan() {
        String kode=txtKode.getText().trim(), nama=txtNama.getText().trim(),
               hjRaw=txtHarga.getText().trim(), stok=txtStok.getText().trim(), sat=txtSatuan.getText().trim();
        KategoriItem kat=(KategoriItem)cmbKategori.getSelectedItem();
        if (kode.isEmpty()||nama.isEmpty()||hjRaw.isEmpty()||stok.isEmpty()||sat.isEmpty()||kat==null) {
            JOptionPane.showMessageDialog(this,"Semua field harus diisi!","Peringatan",JOptionPane.WARNING_MESSAGE); return;
        }
        long harga;
        try { harga=parseRp(hjRaw); if(harga<=0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,"Harga jual tidak valid!\nMasukkan angka saja (contoh: 15000).","Peringatan",JOptionPane.WARNING_MESSAGE);
            txtHarga.requestFocus(); return;
        }
        try {
            Connection conn = Koneksi.getKoneksi();
            PreparedStatement cek = conn.prepareStatement("SELECT id_barang FROM barang WHERE id_barang=?");
            cek.setString(1,kode);
            if (cek.executeQuery().next()) {
                PreparedStatement pst = conn.prepareStatement("UPDATE barang SET nama_barang=?,harga_jual=?,stok=?,satuan=?,id_kategori=? WHERE id_barang=?");
                pst.setString(1,nama); pst.setLong(2,harga); pst.setString(3,stok);
                pst.setString(4,sat);  pst.setString(5,kat.id); pst.setString(6,kode);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this,"Data barang berhasil diperbarui!");
            } else {
                PreparedStatement pst = conn.prepareStatement("INSERT INTO barang(id_barang,nama_barang,harga_jual,stok,satuan,id_kategori) VALUES(?,?,?,?,?,?)");
                pst.setString(1,kode); pst.setString(2,nama); pst.setLong(3,harga);
                pst.setString(4,stok); pst.setString(5,sat);  pst.setString(6,kat.id);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this,"Data barang berhasil disimpan!");
            }
            loadData(); bersihkan();
        } catch (Exception e) { JOptionPane.showMessageDialog(this,"Error simpan: "+e.getMessage()); }
    }

    private void hapus() {
        String kode=txtKode.getText().trim();
        if (kode.isEmpty()) { JOptionPane.showMessageDialog(this,"Pilih barang yang akan dihapus!","Peringatan",JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this,"Yakin ingin menghapus barang dengan kode: "+kode+"?",
                "Konfirmasi Hapus",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement pst=Koneksi.getKoneksi().prepareStatement("UPDATE barang SET status='nonaktif' WHERE id_barang=?");
            pst.setString(1,kode); pst.executeUpdate();
            JOptionPane.showMessageDialog(this,"Data barang berhasil dihapus!");
            loadData(); bersihkan();
        } catch (Exception e) { JOptionPane.showMessageDialog(this,"Error hapus: "+e.getMessage()); }
    }

    private void cari() {
        modelTabel.setRowCount(0);
        try {
            String k="%"+txtCari.getText().trim()+"%";
            PreparedStatement pst=Koneksi.getKoneksi().prepareStatement(
                "SELECT b.id_barang,b.nama_barang,b.harga_jual,b.stok,b.satuan," +
                "COALESCE(k.nama_kategori,'-') AS nama_kategori " +
                "FROM barang b LEFT JOIN kategori k ON b.id_kategori=k.id_kategori " +
                "WHERE (b.id_barang LIKE ? OR b.nama_barang LIKE ?) AND b.status='aktif'");
            pst.setString(1,k); pst.setString(2,k);
            ResultSet rs=pst.executeQuery();
            while (rs.next())
                modelTabel.addRow(new Object[]{rs.getString(1),rs.getString(2),
                    toRp(rs.getDouble(3)),rs.getString(4),rs.getString(5),rs.getString(6)});
        } catch (Exception e) { JOptionPane.showMessageDialog(this,"Error cari: "+e.getMessage()); }
    }

    private void bersihkan() {
        txtNama.setText(""); txtHarga.setText(""); txtStok.setText(""); txtSatuan.setText("");
        if (cmbKategori.getItemCount()>0) cmbKategori.setSelectedIndex(0);
        setHargaErr(null); hargaValid=true;
        btnSimpan.setEnabled(true); btnSimpan.setBackground(C_ACCENT); btnSimpan.repaint();
        tabel.clearSelection();
        txtKode.setText(generateKode());
        txtNama.requestFocus();
    }

    private String generateKode() {
        try {
            ResultSet rs=Koneksi.getKoneksi().prepareStatement(
                "SELECT id_barang FROM barang ORDER BY id_barang DESC LIMIT 1").executeQuery();
            if (rs.next()) return String.format("B%03d",Integer.parseInt(rs.getString(1).replaceAll("[^0-9]",""))+1);
        } catch (Exception ignored) {}
        return "B001";
    }

    // ── DIALOG TAMBAH KATEGORI ────────────────────────────────────────────────
    private void showDialogTambahKategori() {
        JDialog dlg=new JDialog(this,"Tambah Kategori",true);
        dlg.setSize(340,220); dlg.setLocationRelativeTo(this); dlg.setResizable(false);

        JPanel p=new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24,28,24,28)); p.setBackground(Color.WHITE);

        JLabel judul=new JLabel("Tambah Kategori Baru");
        judul.setFont(new Font("Segoe UI",Font.BOLD,15)); judul.setForeground(C_PRIMARY); judul.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl=new JLabel("Nama Kategori");
        lbl.setFont(new Font("Segoe UI",Font.BOLD,11)); lbl.setForeground(new Color(51,65,85)); lbl.setAlignmentX(LEFT_ALIGNMENT);

        JTextField txtKat=new JTextField();
        txtKat.setFont(new Font("Segoe UI",Font.PLAIN,13));
        txtKat.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_BORDER),BorderFactory.createEmptyBorder(6,10,6,10)));
        txtKat.setAlignmentX(LEFT_ALIGNMENT); txtKat.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));

        JButton btnOk=buildBtn("Simpan",C_ACCENT);
        btnOk.setAlignmentX(LEFT_ALIGNMENT); btnOk.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        btnOk.addActionListener(e -> {
            String nama=txtKat.getText().trim();
            if (nama.isEmpty()) { JOptionPane.showMessageDialog(dlg,"Nama kategori tidak boleh kosong!","Peringatan",JOptionPane.WARNING_MESSAGE); return; }
            try {
                Connection conn=Koneksi.getKoneksi();
                String idKat="K001";
                ResultSet rs=conn.createStatement().executeQuery("SELECT id_kategori FROM kategori ORDER BY id_kategori DESC LIMIT 1");
                if (rs.next()) idKat=String.format("K%03d",Integer.parseInt(rs.getString(1).replaceAll("[^0-9]",""))+1);
                PreparedStatement cek=conn.prepareStatement("SELECT id_kategori FROM kategori WHERE nama_kategori=?");
                cek.setString(1,nama);
                if (cek.executeQuery().next()) { JOptionPane.showMessageDialog(dlg,"Kategori \""+nama+"\" sudah ada!","Peringatan",JOptionPane.WARNING_MESSAGE); return; }
                PreparedStatement ins=conn.prepareStatement("INSERT INTO kategori(id_kategori,nama_kategori) VALUES(?,?)");
                ins.setString(1,idKat); ins.setString(2,nama); ins.executeUpdate();
                JOptionPane.showMessageDialog(dlg,"Kategori berhasil ditambahkan!");
                dlg.dispose();
                loadKategori();
                for (int i=0;i<cmbKategori.getItemCount();i++)
                    if (cmbKategori.getItemAt(i).nama.equalsIgnoreCase(nama)) { cmbKategori.setSelectedIndex(i); break; }
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg,"Error: "+ex.getMessage()); }
        });
        txtKat.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) btnOk.doClick(); }
        });

        p.add(judul); p.add(gap(16)); p.add(lbl); p.add(gap(6)); p.add(txtKat); p.add(gap(16)); p.add(btnOk);
        dlg.setContentPane(p); dlg.setVisible(true); txtKat.requestFocus();
    }

    // ── HELPER BUILDERS ───────────────────────────────────────────────────────

    /** Tambah baris form: label → field → [errLabel opsional] */
    private void addRow(JPanel p, String label, JTextField field, JLabel err) {
        JLabel lbl=new JLabel(label); lbl.setFont(new Font("Segoe UI",Font.BOLD,11));
        lbl.setForeground(new Color(51,65,85)); lbl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbl); p.add(gap(4)); p.add(field);
        if (err!=null) { p.add(gap(2)); p.add(err); p.add(gap(6)); } else p.add(gap(10));
    }

    /** Baris kategori: label + (combo | tombol +) */
    private void addRowKategori(JPanel p, boolean isAdmin) {
        JLabel lbl=new JLabel("Kategori"); lbl.setFont(new Font("Segoe UI",Font.BOLD,11));
        lbl.setForeground(new Color(51,65,85)); lbl.setAlignmentX(LEFT_ALIGNMENT);

        JPanel row=new JPanel(new BorderLayout(6,0));
        row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        row.add(cmbKategori, BorderLayout.CENTER);

        if (isAdmin) {
            JButton plus=buildBtn("+", C_ACCENT);
            plus.setPreferredSize(new Dimension(34,34)); plus.setMaximumSize(new Dimension(34,38));
            plus.addActionListener(e -> showDialogTambahKategori());
            row.add(plus, BorderLayout.EAST);
        }

        p.add(lbl); p.add(gap(4)); p.add(row); p.add(gap(10));
    }

    private JTextField buildField() {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean err=Boolean.TRUE.equals(getClientProperty("errorState"));
                g2.setColor(err?C_ERR_BG:(hasFocus()?new Color(240,253,255):new Color(248,250,252)));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.dispose(); super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean err=Boolean.TRUE.equals(getClientProperty("errorState"));
                g2.setColor(err?C_DANGER:(hasFocus()?C_FOCUS:C_BORDER));
                g2.setStroke(new BasicStroke(err||hasFocus()?1.8f:1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose();
            }
        };
        f.setOpaque(false); f.setBorder(new EmptyBorder(6,12,6,12));
        f.setFont(new Font("Segoe UI",Font.PLAIN,13)); f.setForeground(new Color(30,41,59));
        f.setAlignmentX(LEFT_ALIGNMENT); f.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e)   { f.repaint(); }
        });
        return f;
    }

    private JPanel buildCard(int arc) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),arc,arc);
                g2.setColor(new Color(0,0,0,8)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,arc,arc); g2.dispose();
            }
        };
    }

    private JPanel accentLine() {
        JPanel l=new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,C_ACCENT,getWidth(),0,new Color(0,188,212,0)));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        l.setOpaque(false); l.setMaximumSize(new Dimension(Integer.MAX_VALUE,2)); l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JButton buildBtn(String text, Color color) {
        JButton btn=new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,getBackground().brighter(),0,getHeight(),getBackground()));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                if (isEnabled()) { g2.setColor(new Color(255,255,255,35)); g2.fillRoundRect(2,2,getWidth()-4,getHeight()/2,8,8); }
                g2.setColor(isEnabled()?Color.WHITE:new Color(255,255,255,150));
                g2.setFont(getFont()); FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setBackground(color); btn.setFont(new Font("Segoe UI",Font.BOLD,13));
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setPreferredSize(new Dimension(0,42));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if(btn.isEnabled()){btn.setBackground(color.darker());btn.repaint();} }
            @Override public void mouseExited(MouseEvent e)  { if(btn.isEnabled()){btn.setBackground(color);btn.repaint();} }
        });
        return btn;
    }

    private JButton buildTopBtn(String text) {
        JButton btn=new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,20)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(0,188,212,80)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.setColor(Color.WHITE); g2.setFont(getFont()); FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI",Font.BOLD,12)); btn.setFocusPainted(false);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setPreferredSize(new Dimension(110,36));
        return btn;
    }

    private Component gap(int h) { return Box.createRigidArea(new Dimension(0,h)); }

    // ── Functional interface untuk DocumentListener ringkas ───────────────────
    @FunctionalInterface
    private interface SimpleDocumentListener extends DocumentListener {
        void update(DocumentEvent e);
        @Override default void insertUpdate(DocumentEvent e)  { update(e); }
        @Override default void removeUpdate(DocumentEvent e)  { update(e); }
        @Override default void changedUpdate(DocumentEvent e) { update(e); }
    }
}