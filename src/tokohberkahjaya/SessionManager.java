package tokohberkahjaya;

public class SessionManager {
    private static String username;
    private static String namaLengkap;
    private static String level;

    public static void setUser(String username, String namaLengkap, String level) {
        SessionManager.username    = username;
        SessionManager.namaLengkap = namaLengkap;
        SessionManager.level       = level;
    }

    public static String getUsername()    { return username; }
    public static String getNamaLengkap() { return namaLengkap; }
    public static String getLevel()       { return level; }
    public static boolean isAdmin()       { return "Admin".equalsIgnoreCase(level); }

    public static void clear() {
        username    = null;
        namaLengkap = null;
        level       = null;
    }
}