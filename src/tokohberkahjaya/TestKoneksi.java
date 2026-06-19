package tokohberkahjaya;

import java.sql.Connection;

public class TestKoneksi {
    public static void main(String[] args) {
        Connection conn = Koneksi.getKoneksi();

        if (conn != null) {
            System.out.println("Koneksi Berhasil!");
        } else {
            System.out.println("Koneksi Gagal!");
        }
    }
}