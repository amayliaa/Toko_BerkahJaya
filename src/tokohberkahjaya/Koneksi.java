package tokohberkahjaya;

import java.sql.Connection;
import java.sql.DriverManager;

public class Koneksi {
    public static Connection getKoneksi() {
        try {
            String url = "jdbc:mysql://localhost:3306/toko_berkahjaya";
            String user = "root";
            String pass = "";

            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            Connection conn = DriverManager.getConnection(url, user, pass);

            return conn;
        } catch (Exception e) {
            System.out.println("Koneksi Gagal: " + e.getMessage());
            return null;
        }
    }
}